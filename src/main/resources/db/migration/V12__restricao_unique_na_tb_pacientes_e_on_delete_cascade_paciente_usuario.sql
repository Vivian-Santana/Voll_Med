ALTER TABLE pacientes
ADD CONSTRAINT uq_usuario_id UNIQUE (usuario_id);

ALTER TABLE pacientes DROP FOREIGN KEY fk_paciente_usuario;

ALTER TABLE pacientes
ADD CONSTRAINT fk_paciente_usuario
FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
ON DELETE CASCADE;
