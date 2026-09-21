let graficoPorMes = null;

document.addEventListener("DOMContentLoaded", () => {
    const hoje = new Date();
    const umAnoAtras = new Date(hoje.getFullYear() - 1, hoje.getMonth(), hoje.getDate());
    document.getElementById("data-inicio").value = umAnoAtras.toISOString().substring(0, 10);
    document.getElementById("data-fim").value = hoje.toISOString().substring(0, 10);

    document.getElementById("form-filtro").addEventListener("submit", async (evento) => {
        evento.preventDefault();
        await carregarRelatorio();
    });

    carregarRelatorio();
});

async function carregarRelatorio() {
    const inicio = document.getElementById("data-inicio").value;
    const fim = document.getElementById("data-fim").value;

    const relatorio = await Api.get(`/api/relatorios/mensal?inicio=${inicio}&fim=${fim}`);

    document.getElementById("total-viagens").textContent = relatorio.totalViagens;
    preencherTabela("tabela-status", relatorio.porStatus);
    preencherTabela("tabela-motorista", relatorio.porMotorista);
    preencherTabela("tabela-tipo-veiculo", relatorio.porTipoVeiculo);
    desenharGraficoPorMes(relatorio.porMes);
}

function preencherTabela(idTabela, contagens) {
    const corpo = document.getElementById(idTabela);
    corpo.innerHTML = "";
    // sem "for": cada contagem vira uma linha, direto do array que a API devolveu
    contagens
        .map((c) => {
            const linha = document.createElement("tr");
            linha.innerHTML = `<td>${c.rotulo}</td><td class="text-end">${c.quantidade}</td>`;
            return linha;
        })
        .forEach((linha) => corpo.appendChild(linha));
}

function desenharGraficoPorMes(porMes) {
    const contexto = document.getElementById("grafico-por-mes").getContext("2d");
    if (graficoPorMes) {
        graficoPorMes.destroy();
    }
    graficoPorMes = new Chart(contexto, {
        type: "bar",
        data: {
            labels: porMes.map((c) => c.rotulo),
            datasets: [{
                label: "Viagens",
                data: porMes.map((c) => c.quantidade),
                backgroundColor: "#816bad",
            }],
        },
        options: {
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
        },
    });
}
