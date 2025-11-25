INSERT INTO usuarios (id, nome_completo, email, senha_hash, telefone)
VALUES (UUID_TO_BIN('40e6215d-b5c6-4896-987c-030756e83d80'), 'Usuário de Teste', 'teste@aquaguard.com', '$2a$10$N/R1c2WzG/2kZ3E.9w3j2eTmzQ/9u8zS.Y1c1r1b.5UqY6qG6k6f.', '11999999999');

INSERT INTO caixas_dagua (id, usuario_id, nome, capacidade, serial_number, chave_api, criado_em)
VALUES (UUID_TO_BIN('0a7d8b9f-3c1d-4e0a-99b3-5e3e2f7d1a2b'), UUID_TO_BIN('40e6215d-b5c6-4896-987c-030756e83d80'), 'Caixa Principal Teste', 1000.00, 'TEST123456', 'chave-api-teste-123', NOW());

INSERT INTO leituras_volume (caixa_dagua_id, volume_litros, data_hora_leitura) VALUES
(UUID_TO_BIN('0a7d8b9f-3c1d-4e0a-99b3-5e3e2f7d1a2b'), 950.00, '2025-06-28 10:00:00'),
(UUID_TO_BIN('0a7d8b9f-3c1d-4e0a-99b3-5e3e2f7d1a2b'), 925.50, '2025-06-28 18:00:00'),
(UUID_TO_BIN('0a7d8b9f-3c1d-4e0a-99b3-5e3e2f7d1a2b'), 900.00, '2025-06-29 08:00:00');