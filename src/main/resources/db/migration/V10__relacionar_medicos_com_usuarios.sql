ALTER TABLE medicos ADD COLUMN usuario_id BIGINT;

ALTER TABLE medicos
ADD CONSTRAINT fk_medico_usuario
FOREIGN KEY (usuario_id)
REFERENCES usuarios(id);
