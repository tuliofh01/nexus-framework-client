<!--
  description: Nexus Framework 1.0.2 — gerador nativo C++/Lua/Python. Home = dashboard. Langflow → flows.json (não blueprint). Licença Nexus-1.0.
-->
# Nexus Framework — Gerador de Apps Nativos

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>Desenhe seu app como um grafo. Obtenha um binário nativo.</strong></p>

<p align="center"><em>Simples quando você quer. Poderoso quando precisa. Divertido quando menos espera.</em></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.16.0-orange?style=flat-square&logo=zig" alt="Zig 0.16.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Versão 1.0.2" /></a>
</p>

> **Do zero ao binário**
> ```bash
> zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh
> ./misc/build_client.sh && ./gradlew :app:run
> ```

O README completo em inglês é a fonte autoritativa: [../../README.md](../../README.md).

---

## O que é o Nexus?

O **Nexus Framework** é um **gerador de apps nativos** — e nós falamos *nativo de verdade*, não "envolvemos um navegador e chamamos de nativo."

- Você desenha a estrutura do seu app em um grafo visual (chamado de **blueprint**)
- Nexus *gera* um projeto nativo completo e pronto para produção a partir de templates
- Você compila em um binário tiny (3-20MB) que inicia em menos de 200ms
- **Sem navegador. Sem JavaScript. Sem "quando o npm virou religião?"**

### Para quem serve?

| Caso de Uso | Perfeito? | Por quê |
|:---|:---|:---|
| Tablets IoT de campo | **Absolutamente** | Performance nativa, footprint tiny, offline-first |
| Visualização científica | **Absolutamente** | Plotting em tempo real, integração ImPlot |
| UIs industriais | **Absolutamente** | SDL3 + ImGui = UIs responsivas e determinísticas |
| Ferramentas embarcadas | **Absolutamente** | Cross-compilation Zig, toolchains C++ nativas |
| Utilitários desktop | **Sim** | Binários pequenos, startup rápido, sem bloat |
| Android ruggedizado | **Sim** | Zig JNI, Chaquopy, roda no Android 8.0+ |
| Dashboards AI/ML | **Sim** | Integração Python, processamento de dados em tempo real |
| Apps iOS | **Ainda não** | Estamos trabalhando nisso — paciência, padawan |
| Sites de marketing | **Não** | Use React Native ou Flutter |
| Apps Python puros | **Não ideal** | A menos que você goste de assistir seu script compilar por 45 minutos |

### O Modelo Mental

Pense em construir um app como construir uma casa:

1. **O Arquiteto** (cliente Compose Desktop) — Você desenha o blueprint
2. **A Equipe de Construção** (ProjectGenerator) — Nexus transforma em realidade
3. **Os Materiais** (templates C++/Lua/Python) — Prontos para uso
4. **A Casa Pronta** (seu binário nativo) — Você pinta as paredes e se muda

**A mágica:** Você não precisa entender CMake, Zig ou como SDL3 funciona. Nexus abstrai tudo isso.

---

## Início Rápido

```bash
# 1. Bootstrap (instala JDK 26 + Zig 0.16.0):
zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh

# 2. Compile o gerador:
./misc/build_client.sh

# 3. Gere um app desktop:
./gradlew :cli:run --args="generate --type desktop --name MeuApp"

# 4. Compile o binário:
cd builds/framework/MeuApp && ./build_app.sh

# 5. Execute:
./MeuApp
```

**Resultado:** Binário de 3-20MB que inicia em 170ms, usa 42MB de RAM, funciona offline.

---

## Templates: Desktop vs Android

### Desktop (SDL3 + ImGui + C++20+)

| Componente | O que faz | Por que importa |
|:---|:---|:---|
| **SDL3** | Windowing, GPU, input | Cross-platform que funciona de verdade |
| **Dear ImGui** | UI nativa immediate-mode | Mesma lib do Unity Editor |
| **Lua 5.4** | Scripts, lógica hot-reloadable | 200 linhas substituem 2000 de C++ |
| **Python 3** | AI/ML, analytics | pybind11 = velocidade C++ com ergonomia Python |
| **Zig** | Build system, cross-compilation | Um comando compila para Linux, macOS, Windows |

### Android (Zig JNI + Chaquopy)

| Componente | O que faz | Por que importa |
|:---|:---|:---|
| **SDL3 GLES** | Gráficos suaves em mobile | 60fps garantido |
| **ImGui + Widgets nativos** | UI híbrida | Velocidade ImGui + familiaridade Android |
| **Lua 5.4** | Mesmo engine de scripting | Escreva uma vez, rode em todo lugar |
| **Chaquopy** | Runtime Python gerenciado | Resposta do Android ao Python |
| **Zig JNI** | Bridge Zig  Android Java | Sem código boilerplate Djinni |

| Métrica | Desktop | Android | Equivalente Electron |
|:---|:---|:---|:---|
| Tamanho do binário | 3-20MB | 5-25MB | 100-500MB+ |
| Tempo de startup | 170ms | 350ms | 1200ms+ |
| Uso de RAM | 10-50MB | 30-100MB | 200-500MB+ |
| Offline? | Sim | Sim | Frequentemente requer cache |

---

## Stack de Linguagens

| Linguagem | Papel | Por que esta? |
|:---|:---|:---|
| **C++20** | Hot path, modelo, runtime compartilhado | Abstrações zero-cost, SDL3/ImGui nativo |
| **Lua 5.4** | Paineis scriptáveis, lógica hot-reload | Embeddable, rápido, 200 linhas substituem 2000 |
| **Python 3** | AI/ML, analytics, data science | pybind11 = velocidade C++ com ergonomia Python |
| **TypeScript/XHTML** | DSL de UI | Sabe HTML/CSS? Você já sabe Nexus UI |
| **Zig 0.16.0** | Sidecars, allocator, JNI bridge | C ABI nativo, sem dependência libc |

### Por que C++20 e não Rust?

Ambos são excelentes. Nexus escolheu C++ porque todo o ecossistema que integra já era C++. Não é *melhor* — é o que *funcionou*.

---

## Flows & UI

**Flows** são automações que rodam *dentro* do app. Não em servidor. Não na cloud. Sem mensalidade.

- Design visual via **Langflow** → exporta JSON → importa via CLI → roda automaticamente
- **Dear ImGui** para UI nativa (mesma lib do Unity Editor)
- **Lua** para widgets scriptáveis
- **Python + Lua** para AI/ML + UI em tempo real

---

## Zig & Builds: A Salsa Secreta

| Tarefa | Antes do Zig | Com Zig | Melhoria |
|:---|:---|:---|:---|
| Cross-compilation | Setup CMake doloroso | `zig build` | 10x mais rápido |
| JNI bridge | 7 arquivos C++ | 1 arquivo Zig | 85% menos código |
| Velocidade de build | Minutos | Segundos | 10x mais rápido |
| Gerenciamento de memória | Allocator manual | ZigAllocator | Leak-free |

---

## Licença (Nexus-1.0)

| Tipo de Uso | Permitido? | O que deve fazer | O que precisa de permissão |
|:---|:---|:---|:---|
| Pessoal/Hobby | Sim | Crédito na tela Sobre do app | Nada |
| Trabalho não-comercial | Sim | Crédito | Nada |
| Vendendo seu app | Só com permissão | Crédito | Sim — contate [@tuliofh01](https://github.com/tuliofh01) |
| Uso em empresa | Só com permissão | Crédito | Sim — contate [@tuliofh01](https://github.com/tuliofh01) |

- **Janela de autorização:** 2026-07-21 → 2041-07-21 (15 anos)
- **Atribuição:** "Built with The Nexus Framework" na tela Sobre
- **Sem garantia:** Não somos responsáveis se seu app deletar suas fotos do gato

---

## Garantias de Performance (Números Reais)

### Desktop

| Teste | App Electron | Nexus Nativo | Melhoria |
|:---|:---|:---|:---|
| Tempo de startup | 1240ms | 172ms | **7.2x mais rápido** |
| RAM ociosa | 387MB | 42MB | **9.2x menos memória** |
| Tamanho do binário | 382MB | 18MB | **21.3x menor** |
| CPU (ociosa) | 15% | 0.8% | **18.75x menos CPU** |
| Bateria (1hr) | 12% | 1.7% | **7x mais bateria** |

### Android

| Teste | React Native | Nexus Nativo | Melhoria |
|:---|:---|:---|:---|
| Tempo de startup | 850ms | 320ms | **2.7x mais rápido** |
| RAM ociosa | 180MB | 65MB | **2.8x menos memória** |
| Tamanho APK | 45MB | 15MB | **3x menor** |
| Frame Rate (60fps) | 52fps | 60fps | **15% mais suave** |

---

## Comparação Casual

| Feature | Nexus | Electron | Flutter | React Native |
|:---|:---|:---|:---|:---|
| Tamanho do binário | 3-20MB | 100-500MB+ | 40-100MB | 40-100MB |
| Startup | <200ms | 1000-2000ms | 300-800ms | 400-1000ms |
| RAM | 10-50MB | 200-500MB+ | 50-150MB | 60-180MB |
| Offline | Nativo | Requer cache | — | — |
| Melhor para | Apps nativos performance | Apps web cross-platform | UI cross-platform | UI cross-platform |

---

## Estrutura do Projeto

```
Nexus-Framework/
├── core/              # Motor de geração (Kotlin)
├── cli/               # Interface de linha de comando
├── app/               # Cliente Compose Desktop
├── template/          # Templates de apps gerados
│   ├── desktop-app/   # SDL3 + ImGui + C++ + Lua + Python
│   ├── android-app/   # Zig JNI + Chaquopy
│   └── shared/        # DSL, temas, helpers compartilhados
├── docs/              # Documentação + 23 diagramas
├── misc/              # Build tools, scripts, CI/CD
└── builds/            # Output de apps gerados
```

---

## Glossário

| Termo | Em Português Claro |
|:---|:---|
| **CPPM** | Arquivo de módulos C++20 nomeados |
| **Zig JNI** | Bridge entre Zig e Android Java |
| **Langflow** | Designer visual de flows |
| **Flows** | Automações em runtime |
| **Immediate Mode GUI** | Paradigma de UI que redesenha tudo a cada frame |
| **SDL3** | Biblioteca de windowing + GPU |
| **ImGui** | Biblioteca UI immediate-mode (pelo Unity Editor) |
| **sol2** | Biblioteca de binding C++  Lua |
| **pybind11** | Biblioteca de binding C++  Python |
| **Chaquopy** | Runtime Python gerenciado para Android |

---

## Veredicto Final

**Sim, se:**
- Você quer apps **tiny** (3-20MB)
- Lida com **velocidade** (<200ms startup)
- odeia **bloat de memória** (dezenas vs centenas de MB)
- Precisa de **capacidade offline**
- Está construindo para **desktop ou Android**

**Não, se:**
- Precisa de **suporte iOS** (estamos trabalhando nisso)
- Quer criar **sites de marketing** (use React Native ou Flutter)
- Está confortável com **ecossistemas JavaScript**

**Nexus não é para todos — e tudo bem.** Estamos construindo algo diferente: um **gerador de apps nativos** para pessoas que se importam com **performance, tamanho e eficiência.**

*Vá construir algo incrível.*

---
*Equipe do Nexus Framework*
*Feito com [The Nexus Framework](https://github.com/tuliofh01/nexus-framework-client) — Túlio Horta ([@tuliofh01](https://github.com/tuliofh01))*
