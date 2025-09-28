const ModuleLabels = {
  NEW_REGISTRATION: "New Registration",
  NEW_EDITION: "New Edition",
  REVISED_REGISTRATION: "Revised Registration",
  OWNERSHIP: "Ownership Transfer",
  DISCONTINUATION_OF_PUBLICATION: "Discontinuation of Publication",
  NEWSPRINT_DECLARATION_AUTHENTICATION: "Newsprint Declaration Authentication"
};

// 🔹 Admin (long labels) & User (short labels)
const StatusLabels = {
  NEW_APPLICATION: [
    "New Applications (Response awaited from Specified Authority within 60 days window)",
    "New Applications"
  ],
  APPLICATION_RECEIVED_FROM_SA: [
    "Applications received from Specified Authority with/without comments after 60 days",
    "Applications Received"
  ],
  DEFICIENT_AWAITING_PUBLISHER: [
    "Deficient – Applications Response awaited from publishers",
    "Deficient Applications"
  ],
  UNDER_PROCESS_AT_PRGI: [
    "Under Process at PRGI (Above ASO Level)",
    "Under Process"
  ],
  APPLICATION_REJECTED: [
    "Applications Rejected",
    "Rejected"
  ],
  REGISTRATION_GRANTED: [
    "Registration Granted",
    "Granted"
  ]
};

const StatusOrder = [
  "NEW_APPLICATION",
  "APPLICATION_RECEIVED_FROM_SA",
  "DEFICIENT_AWAITING_PUBLISHER",
  "UNDER_PROCESS_AT_PRGI",
  "APPLICATION_REJECTED",
  "REGISTRATION_GRANTED"
];

const ModuleOrder = Object.keys(ModuleLabels);

// Default all "-"
const DefaultSummary = {};
ModuleOrder.forEach(mKey => {
  DefaultSummary[ModuleLabels[mKey]] = {};
  StatusOrder.forEach(s => {
    DefaultSummary[ModuleLabels[mKey]][StatusLabels[s][0]] = "-";
  });
});

let CurrentSummary = JSON.parse(JSON.stringify(DefaultSummary));

function normalizeKey(str) {
  return str?.toLowerCase().replace(/\s+/g, " ").trim();
}

function buildShell() {
  const container = document.getElementById("modules");
  container.innerHTML = "";
  ModuleOrder.forEach(mKey => {
    const section = document.createElement("section");
    section.className = `module ${mKey}`;
    section.innerHTML = `
      <h2>
        <span>${ModuleLabels[mKey]}</span>
        <span class="toggle-icon">+</span>
      </h2>
      <div class="grid" id="grid-${mKey}"></div>
    `;
    container.appendChild(section);

    const grid = section.querySelector(".grid");
    StatusOrder.forEach(s => {
      const id = `${mKey}_${s}`;
      const card = document.createElement("div");
      card.className = `card ${s}`;
      card.id = `card-${id}`;
      card.innerHTML = `
        <div class="status">${StatusLabels[s][0]}</div>
        <div class="count" id="count-${id}">${CurrentSummary[ModuleLabels[mKey]][StatusLabels[s][0]]}</div>
      `;
      grid.appendChild(card);
    });
  });
}

function updateCardValue(moduleKey, statusKey, newValue) {
  const count = document.getElementById(`count-${moduleKey}_${statusKey}`);
  if (count) {
    count.textContent = newValue ?? "-";
  }
}

function loadSummary() {
  return fetch("/api/applications/summary", {
    headers: { "Authorization": "Bearer " + localStorage.getItem("jwtToken") }
  })
      .then(r => {
        if (!r.ok) throw new Error("API error: " + r.status);
        return r.json();
      })
      .then(summary => {
        console.log("API Summary:", summary);

        CurrentSummary = JSON.parse(JSON.stringify(DefaultSummary));

        ModuleOrder.forEach(mKey => {
          const moduleName = ModuleLabels[mKey];
          const apiObj = summary[moduleName];
          if (apiObj) {
            StatusOrder.forEach(s => {
              const labels = StatusLabels[s];
              let apiValue = "-";

              labels.forEach(label => {
                if (apiObj[label] !== undefined) {
                  apiValue = apiObj[label];
                } else {
                  const foundKey = Object.keys(apiObj).find(
                      k => normalizeKey(k) === normalizeKey(label)
                  );
                  if (foundKey) apiValue = apiObj[foundKey];
                }
              });

              CurrentSummary[moduleName][labels[0]] = apiValue;
              updateCardValue(mKey, s, apiValue);
            });
          }
        });

        enableTileClicks(summary);
      })
      .catch(err => {
        console.error("summary error:", err);
        CurrentSummary = JSON.parse(JSON.stringify(DefaultSummary));
        buildShell();
      });
}

// 🔹 Check if admin by looking for long keys
function isAdminSummary(summary) {
  return Object.keys(summary["New Registration"] || {}).some(k =>
      k.includes("Specified Authority")
  );
}

function enableTileClicks(summary) {
  const isAdmin = isAdminSummary(summary);

  // remove handlers for everyone first
  document.querySelectorAll(".card").forEach(card => {
    card.onclick = null;
    card.style.cursor = isAdmin ? "pointer" : "not-allowed";
  });

  if (!isAdmin) {
    console.log("Normal user → tiles disabled (numbers only)");
    return;
  }

  console.log("Admin → enabling tile clicks...");

  // ✅ Fixed API URLs
  document.getElementById("card-NEW_REGISTRATION_NEW_APPLICATION").onclick =
      () => fetchAndShow("/api/applications/new-registration/new-applications", "🆕 New Applications");

  document.getElementById("card-NEW_REGISTRATION_DEFICIENT_AWAITING_PUBLISHER").onclick =
      () => fetchAndShow("/api/applications/new-registration/deficient", "⚠️ Deficient Applications");
}

function fetchAndShow(url, title) {
    document.getElementById("modalTitle").textContent = title;
    document.getElementById("modalBody").innerHTML =
        "<div class='spinner-container'><div class='spinner'></div></div>";
    modal.style.display = "block";
    document.body.style.overflow = "hidden";

    fetch(url, { headers: { "Authorization": "Bearer " + token } })
        .then(r => {
            if (r.status === 401) throw new Error("Unauthorized");
            if (r.status === 403) throw new Error("Forbidden");
            return r.json();
        })
        .then(data => {
            document.getElementById("modalBody").innerHTML = buildTable(data);

            // 🔹 Show Excel button only for ADMIN
            const role = localStorage.getItem("userRole");
            if (role === "ROLE_ADMIN" && data.length > 0) {
                modalExcelBtn.style.display = "inline-block";
                modalExcelBtn.onclick = () => exportModalTableToExcel(title);
            } else {
                modalExcelBtn.style.display = "none";
            }
        })
        .catch(err => {
            console.error("Error:", err);
            modalExcelBtn.style.display = "none"; // hide button on error too

            if (err.message === "Unauthorized") {
                logout();
            } else if (err.message === "Forbidden") {
                document.getElementById("modalBody").innerHTML =
                    `<p style="text-align:center; color: red; font-size:16px; padding:40px;">
                        🚫 You do not have permission to view these details.
                    </p>`;
            } else {
                showNotification("Something went wrong while fetching data.", "error");
                closeModal();
            }
        });
}
function buildTable(data) {
  if (!data || data.length === 0) return "<p>No records found.</p>";
  let cols = Object.keys(data[0]);
  let html = "<div class='table-wrapper'><table id='modalTable'><thead><tr>";
  cols.forEach(c => (html += `<th>${c}</th>`));
  html += "</tr></thead><tbody>";
  data.forEach(row => {
    html += "<tr>";
    cols.forEach(c => (html += `<td>${row[c] ?? ""}</td>`));
    html += "</tr>";
  });
  html += "</tbody></table></div>";
  return html;
}

function exportModalTableToExcel(title) {
  const table = document.getElementById("modalTable");
  if (!table) return;
  const wb = XLSX.utils.book_new();
  const ws = XLSX.utils.table_to_sheet(table);
  XLSX.utils.book_append_sheet(wb, ws, "Data");
  XLSX.writeFile(wb, `${title.replace(/\s+/g, "_")}.xlsx`);
}

function exportToExcel() {
  let wb = XLSX.utils.book_new();
  let header = [
    "S.No.",
    "Nature of Application",
    StatusLabels.NEW_APPLICATION[0],
    StatusLabels.APPLICATION_RECEIVED_FROM_SA[0],
    StatusLabels.DEFICIENT_AWAITING_PUBLISHER[0],
    StatusLabels.UNDER_PROCESS_AT_PRGI[0],
    StatusLabels.APPLICATION_REJECTED[0],
    StatusLabels.REGISTRATION_GRANTED[0]
  ];
  let rows = [];
  let serial = 1;
  ModuleOrder.forEach(mKey => {
    const moduleName = ModuleLabels[mKey];
    const summary = CurrentSummary[moduleName];
    rows.push({
      "S.No.": serial++,
      "Nature of Application": moduleName,
      [StatusLabels.NEW_APPLICATION[0]]: summary[StatusLabels.NEW_APPLICATION[0]],
      [StatusLabels.APPLICATION_RECEIVED_FROM_SA[0]]: summary[StatusLabels.APPLICATION_RECEIVED_FROM_SA[0]],
      [StatusLabels.DEFICIENT_AWAITING_PUBLISHER[0]]: summary[StatusLabels.DEFICIENT_AWAITING_PUBLISHER[0]],
      [StatusLabels.UNDER_PROCESS_AT_PRGI[0]]: summary[StatusLabels.UNDER_PROCESS_AT_PRGI[0]],
      [StatusLabels.APPLICATION_REJECTED[0]]: summary[StatusLabels.APPLICATION_REJECTED[0]],
      [StatusLabels.REGISTRATION_GRANTED[0]]: summary[StatusLabels.REGISTRATION_GRANTED[0]]
    });
  });
  let totalRow = { "S.No.": "", "Nature of Application": "Total" };
  header.slice(2).forEach(label => {
    let sum = 0, numeric = true;
    ModuleOrder.forEach(mKey => {
      let val = CurrentSummary[ModuleLabels[mKey]][label];
      if (!isNaN(val)) sum += Number(val);
      else numeric = false;
    });
    totalRow[label] = numeric ? sum : "";
  });
  rows.push(totalRow);
  let ws = XLSX.utils.json_to_sheet(rows, { header });
  XLSX.utils.book_append_sheet(wb, ws, "Application Summary");
  XLSX.writeFile(wb, "Application_Summary.xlsx");
}

document.addEventListener("DOMContentLoaded", () => {
  buildShell();
  const applyBtn = document.getElementById("btnApply");
  const excelBtn = document.getElementById("btnExcel");
  if (applyBtn) {
    applyBtn.addEventListener("click", () => {
      loadSummary().then(() => {
        excelBtn.style.display = "inline-block";
      });
    });
  }
  if (excelBtn) {
    excelBtn.addEventListener("click", exportToExcel);
  }
});
