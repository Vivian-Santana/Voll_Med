package med.voll.api.domain.medico;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Especialidade {

	ORTOPEDIA, CARDIOLOGIA, GINECOLOGIA, OBSTETRICIA, DERMATOLOGIA, OFTALMOLOGIA, HEMATOLOGIA, PEDIATRIA, GERIATRIA,
	NEUROLOGIA, OTORRINOLARINGOLOGIA, ALERGOLOGIA, PNEUMOLOGIA, GASTROLOGIA, UROLOGIA, NEFROLOGIA, VASCULAR, ONCOLOGIA,
	PSIQUIATRIA, PSICOLOGIA, CLINICO;

	@JsonCreator
	public static Especialidade fromValue(String value) {
		return Especialidade.valueOf(value.toUpperCase());
	}
}
