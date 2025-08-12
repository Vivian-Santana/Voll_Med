ALTER TABLE medicos
ADD CONSTRAINT uq_usuario_id UNIQUE (usuario_id);

ALTER TABLE medicos DROP FOREIGN KEY fk_medico_usuario;

ALTER TABLE medicos
ADD CONSTRAINT fk_medico_usuario
FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
ON DELETE CASCADE;
