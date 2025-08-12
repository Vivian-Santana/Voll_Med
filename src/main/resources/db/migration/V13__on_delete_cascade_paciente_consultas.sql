ALTER TABLE consultas
DROP FOREIGN KEY fk_consultas_paciente_id;

ALTER TABLE consultas
ADD CONSTRAINT fk_consultas_paciente_id
FOREIGN KEY (paciente_id) REFERENCES pacientes(id)
ON DELETE CASCADE;
