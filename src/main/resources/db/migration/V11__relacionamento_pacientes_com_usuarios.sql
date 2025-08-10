ALTER TABLE pacientes
ADD COLUMN usuario_id BIGINT;

ALTER TABLE pacientes
ADD CONSTRAINT fk_paciente_usuario
FOREIGN KEY (usuario_id) 
REFERENCES usuarios(id);
