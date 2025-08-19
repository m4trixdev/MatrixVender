# MatrixVender

MatrixVender é um plugin de Minecraft que permite criar um menu de venda de itens para jogadores, com suporte a economia e notificações personalizáveis. Funciona do **Minecraft 1.8 até a 1.21** e é totalmente configurável pelo `config.yml`.

---

## Funcionalidades

- Menu de venda interativo com slots bloqueados e liberáveis conforme permissão.  
- Configuração de itens vendáveis e seus preços diretamente no `config.yml`.  
- Multiplicadores de venda por permissão (1.2x, 1.5x, 2x, 3x).  
- Notificações de venda por chat, actionbar ou title.  
- Devolução automática de itens não vendáveis.  
- Compatível com qualquer plugin de economia (Vault ou custom).  

---

## Comandos

- `/vender` - Abre o menu de venda para o jogador.  

---

## Permissões

- `matrixvender.slot.20` - Libera 20 slots de venda.  
- `matrixvender.slot.30` - Libera 30 slots de venda.  
- `matrixvender.slot.40` - Libera 36 slots de venda.  
- `matrixvender.multiplier.1.2` - Multiplicador 1.2x nas vendas.  
- `matrixvender.multiplier.1.5` - Multiplicador 1.5x nas vendas.  
- `matrixvender.multiplier.2.0` - Multiplicador 2x nas vendas.  
- `matrixvender.multiplier.3.0` - Multiplicador 3x nas vendas.  

---

## Configuração

No `config.yml` você pode definir:

- Mensagens do menu, título, lore e notificações.  
- Quais itens podem ser vendidos e seus preços.  
- Quantidade de slots padrão para jogadores sem permissão.  

Exemplo de `config.yml`:

```yaml
venda:
  slots-padrao: 9
  itens:
    DIAMOND: 50.0
    IRON_INGOT: 10.0

mensagens:
  menu-titulo: "&8Vender"
  info-titulo: "&aMenu de Venda"
  info-lore:
    - "&7Slots disponíveis: {slots}"
    - "&7Multiplicador: {multiplier}x"
  slot-bloqueado: "&cSlot Bloqueado"
  slot-bloqueado-lore: "&7Adquira mais slots"
  itens-devolvidos: "&e{quantidade} itens não vendáveis foram devolvidos"

notificacoes:
  tipo-venda: "chat" # chat, title, actionbar
  chat:
    ativado: true
    formato: "&aVocê vendeu {quantidade} itens por ${valor}"
  title:
    ativado: true
    titulo: "&aVenda Realizada!"
    subtitulo: "&e{quantidade} itens por ${valor}"
  actionbar:
    ativado: true
    formato: "&aVenda: {quantidade} itens por ${valor}"
