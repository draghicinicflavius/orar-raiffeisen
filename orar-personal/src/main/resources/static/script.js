let allSchedules = []; // Salvăm toate datele aici ca să nu mai cerem la server mereu
let currentViewMonth = new Date().getMonth() + 1; // Implicit luna curentă (ex: 4 pentru Aprilie)

function checkLogin() {
    const u = document.getElementById('userInput').value;
    const p = document.getElementById('passInput').value;
    if (u === 'flavius' && p === 'admin') { showApp(true); } 
    else if (u === 'TL12' && p === 'orar') { showApp(false); } 
    else { document.getElementById('loginError').style.display = 'block'; }
}

function showApp(isAdmin) {
    document.getElementById('loginScreen').style.display = 'none';
    document.getElementById('mainApp').style.display = 'block';
    if (isAdmin) document.getElementById('adminZone').style.display = 'flex';
    loadData();
}

async function loadData() {
    try {
        const response = await fetch('/api/orar/toate');
        allSchedules = await response.json();
        renderDisplay(); // Afișăm datele
    } catch (e) {
        console.error("Eroare server:", e);
    }
}

// Funcția care desenează cardurile pe ecran în funcție de lună
function renderDisplay() {
    const resultsDiv = document.getElementById('results');
    const today = new Date();
    const todayStr = today.toLocaleDateString('ro-RO', { day: 'numeric', month: 'short' });

    // Filtrăm doar datele din luna selectată
    const filteredData = allSchedules.filter(item => {
        const d = new Date(item.workDate);
        return (d.getMonth() + 1) === currentViewMonth;
    });

    if (filteredData.length === 0) {
        resultsDiv.innerHTML = '<div style="text-align:center; padding:40px; color:#666;"><h3>Nu există orar pentru această lună.</h3></div>';
        return;
    }

    const grouped = {};
    filteredData.forEach(item => {
        if(!grouped[item.agentName]) grouped[item.agentName] = [];
        grouped[item.agentName].push(item);
    });

    resultsDiv.innerHTML = '';
    for (let agent in grouped) {
        let card = `<div class="agent-card" data-agent="${agent.toUpperCase()}">
                        <div class="agent-name">${agent}</div>
                        <div class="days-container">`;
        
        grouped[agent].sort((a,b) => new Date(a.workDate) - new Date(b.workDate));

        grouped[agent].forEach(day => {
            let isOff = day.shiftDetails.toUpperCase().includes("OFF");
            let d = new Date(day.workDate);
            let formattedDate = d.toLocaleDateString('ro-RO', { day: 'numeric', month: 'short' });
            let isToday = (formattedDate === todayStr && (d.getMonth() + 1) === (today.getMonth() + 1));

            card += `<div class="day-item ${isToday ? 'today-highlight' : ''}">
                        <div class="date-label">${isToday ? 'AZI' : formattedDate}</div>
                        <span class="${isOff ? 'off-day' : 'shift-info'}">${day.shiftDetails}</span>
                     </div>`;
        });
        card += `</div></div>`;
        resultsDiv.innerHTML += card;
    }
    autoScrollToToday();
}

// Funcția apelată de butoanele de luni
function filterByMonth(month, btn) {
    currentViewMonth = month;
    // Mutăm clasa "active" pe butonul apăsat
    document.querySelectorAll('.month-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    renderDisplay();
}

function autoScrollToToday() {
    setTimeout(() => {
        const containers = document.querySelectorAll('.days-container');
        containers.forEach(container => {
            const todayElement = container.querySelector('.today-highlight');
            if (todayElement) {
                container.scrollTo({ left: todayElement.offsetLeft - container.offsetLeft - 20, behavior: 'smooth' });
            }
        });
    }, 300);
}

function filterData() {
    let input = document.getElementById('searchInput').value.toUpperCase();
    let cards = document.getElementsByClassName('agent-card');
    for (let i = 0; i < cards.length; i++) {
        let name = cards[i].getAttribute('data-agent');
        cards[i].style.display = name.includes(input) ? "" : "none";
    }
}
async function deleteCurrentMonth() {
    const luni = ["", "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"];
    const numeLuna = luni[currentViewMonth];

    if (confirm(`Ești sigur că vrei să ștergi TOATE datele din luna ${numeLuna}?`)) {
        try {
            // Trimitem cererea către server cu luna și anul curent
            const year = new Date().getFullYear();
            const response = await fetch(`/api/orar/sterge-luna?month=${currentViewMonth}&year=${year}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                alert(`Datele pentru luna ${numeLuna} au fost șterse!`);
                loadData(); // Reîncărcăm datele de la server
            } else {
                alert("Eroare la ștergere.");
            }
        } catch (error) {
            console.error("Eroare:", error);
        }
    }
}

function logout() { location.reload(); }