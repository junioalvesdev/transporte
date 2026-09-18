/**
 * Mapeia o nome de um tipo de veiculo pro icone mais parecido do Bootstrap
 * Icons — puramente visual (nao afeta nada que e enviado ao backend).
 */
function iconeParaTipoVeiculo(tipo) {
    const nome = (tipo || "").toLowerCase();
    if (nome.includes("caminh")) return "bi-truck";
    if (nome.includes("moto")) return "bi-scooter";
    if (nome.includes("onibus") || nome.includes("bus")) return "bi-bus-front-fill";
    if (nome.includes("ambul")) return "bi-truck-front-fill";
    if (nome.includes("van") || nome.includes("kombi") || nome.includes("furg") || nome.includes("minibus")) return "bi-truck-front-fill";
    return "bi-car-front-fill";
}
