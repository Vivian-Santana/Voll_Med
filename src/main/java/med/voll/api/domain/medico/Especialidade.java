package med.voll.api.domain.medico;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Especialidade {

	ORTOPEDIA, CARDIOLOGIA, GINECOLOGIA, OBSTETRICIA, DERMATOLOGIA, OFTALMOLOGIA, HEMATOLOGIA, PEDIATRIA, GERIATRIA,
	NEUROLOGIA, OTORRINOLARINGOLOGIA, ALERGOLOGIA, PNEUMOLOGIA, GASTROENTEROLOGIA, UROLOGIA, NEFROLOGIA, VASCULAR, ONCOLOGIA,
	PSIQUIATRIA, PSICOLOGIA, CLINICO, CIRURGIA_GERAL;

	@JsonCreator
	public static Especialidade fromValue(String value) {
		return Especialidade.valueOf(value.toUpperCase());
	}
	
	@Override
    @JsonValue
    public String toString() {
        // Substitui underscores por espaços e deixa apenas a primeira letra maiúscula
        String formatted = name().toLowerCase().replace("_", " ");
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}
