# 🔧 Guia do Número de Série (Serial) da Caixa d'Água

## 💡 O que é o número de série?

O **número de série** é um identificador único que vem gravado no dispositivo IoT físico que monitora sua caixa d'água. É como o "RG" do seu dispositivo!

## 📍 Onde encontrar o número de série?

### 🏷️ Em dispositivos reais:
- **Etiqueta no dispositivo**: Colada na parte traseira ou lateral do sensor
- **QR Code**: Muitos dispositivos têm um QR code que você pode escanear
- **Manual**: Vem na documentação que acompanha o dispositivo
- **Caixa do produto**: Às vezes está impresso na embalagem

### 🧪 Para desenvolvimento/teste:
- **Botão "Gerar Serial de Teste"**: No app, ao adicionar uma caixa, use este botão para gerar um serial válido para testes
- **Formato comum**: Geralmente algo como `AQ001234`, `TEST123456` ou similar

## 🔄 Como usar no app:

1. **Abra o app** e vá para a aba "Caixas"
2. **Toque em "Adicionar Caixa"**
3. **No campo "Serial do Dispositivo"**:
   - Digite o serial que está no seu dispositivo físico, OU
   - Toque em "🎲 Gerar Serial de Teste" para desenvolvimento
4. **Preencha os outros campos** (nome, capacidade, etc.)
5. **Toque em "Criar"**

## ⚠️ Problemas comuns:

### "Dispositivo com este serial já foi cadastrado"
- **Causa**: O serial já está sendo usado por outro usuário
- **Solução**: Verifique se digitou corretamente ou gere um novo serial de teste

### "Serial inválido"
- **Causa**: Formato incorreto ou caracteres especiais
- **Solução**: Use apenas letras e números, sem espaços ou símbolos

## 🚀 Desenvolvimento e Testes:

Para desenvolvedores e testes, você pode:

1. **Usar o botão de gerar serial** no próprio app
2. **Fazer uma chamada para a API**:
   ```
   GET /api/v1/caixas-dagua/gerar-serial-teste
   ```
3. **Gerar manualmente**: Use formato `TEST` + timestamp (ex: `TEST1674576234`)

## 🔐 Segurança:

- Cada serial é **único** no sistema
- Uma vez pareado, o dispositivo fica **vinculado à sua conta**
- Apenas você pode ver e controlar dispositivos com seriais que você cadastrou

---

**💡 Dica**: Em produção, o serial sempre vem do fabricante do dispositivo. Para testes e desenvolvimento, use a função de gerar serial automático do app!
