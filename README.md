# CG2024-java-GB

## Proposta do Projeto

Este projeto é um **visualizador 3D** para cenas tridimensionais, desenvolvido em **Java** utilizando **LWJGL (Lightweight Java Game Library)** e **JOML (Java OpenGL Math Library)**. Ele permite carregar modelos 3D no formato `.obj` com suas texturas, aplicar transformações geométricas (translação, rotação, escala) e visualizar a cena com iluminação baseada no modelo Phong.

### Funcionalidades

1. **Carregamento de Objetos 3D**: Suporte para modelos `.obj` com texturas e materiais.
2. **Transformações**:
   - Movimentação, rotação e escala de objetos.
   - Animações baseadas em curvas paramétricas (ex.: Bézier).
3. **Iluminação**:
   - Implementação do modelo de iluminação Phong.
   - Suporte para coeficientes `ka`, `kd`, `ks` definidos por material.
4. **Controle de Câmera**:
   - Navegação pela cena usando teclado e/ou mouse.
5. **Configuração Dinâmica**:
   - Configuração da cena a partir de um arquivo JSON.

## Tecnologias Utilizadas

- **Java 17**: Linguagem de programação principal.
- **LWJGL 3.3.1**: Biblioteca para interação com OpenGL, GLFW, e Assimp.
- **JOML 1.10.5**: Biblioteca matemática para operações 3D.
- **Jackson Databind**: Para manipulação e leitura de arquivos JSON.

## Requisitos do Sistema

1. **Sistema Operacional**: Windows
2. **JDK**: Java 17 ou superior
3. **Maven**: Para gerenciamento de dependências e build.
4. **GPU**: Compatível com OpenGL 3.3 ou superior.

## Configuração do Ambiente

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/CG2024-java-GB.git
cd CG2024-java-GB
```

### 2. Configure o Ambiente Java

Certifique-se de que o **JDK 17** esteja instalado e configurado no seu PATH.

### 3. Build com Maven

Compile o projeto e resolva as dependências:

```bash
mvn clean install
```

### 4. Configuração do Arquivo de Cena

Crie ou edite um arquivo de cena JSON em `src/main/resources/scenes/scene.json`. Exemplo:
```json
{
  "objects": [
    {
      "modelPath": "models\\Suzannes\\Suzanne.obj",
      "position": [0.0, 0.0, 0.0],
      "scale": [1.0, 1.0, 1.0],
      "rotation": [0.0, 0.0, 0.0],
      "animation": {
        "type": "bezier",
        "points": [
          [0.0, 0.0, 0.0],
          [1.0, 2.0, 0.0],
          [2.0, -1.0, 0.0],
          [3.0, 0.0, 0.0]
        ]
      }
    },
    {
      "modelPath": "models\\Navezinha\\Nave.obj",
      "position": [0.0, 0.0, 0.0],
      "scale": [1.0, 1.0, 1.0],
      "rotation": [0.0, 0.0, 0.0],
      "animation": {
        "type": "bezier",
        "points": [
          [0.0, 0.0, 0.0],
          [1.0, 2.0, 0.0],
          [2.0, -1.0, 0.0],
          [3.0, 0.0, 0.0]
        ]
      }
    }
  ],
  "lights": [
    {
      "position": [2.0, 2.0, 2.0],
      "color": [1.0, 1.0, 1.0]
    }
  ],
  "camera": {
    "position": [0.0, 0.0, 5.0],
    "fov": 45,
    "near": 0.1,
    "far": 100.0
  }
}

```

### 5. Executando o Projeto

Rode o projeto com o Maven:

```bash
mvn exec:java -Dexec.mainClass="Main"
```

Ou execute diretamente com Java:

```bash
java -cp target/CG2024-java-GB-1.0-SNAPSHOT.jar Main
```

## Controles

- **Teclas Numéricas (1, 2, 3...)**: Seleciona objetos.
- **W, A, S, D, Q, E**: Movimenta o objeto selecionado.
- **R**: Rotaciona o objeto selecionado no eixo Y.
- **+ e -**: Aumenta ou diminui a escala do objeto selecionado.

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   ├── loader/         # Classes para carregar arquivos OBJ e JSON
│   │   ├── renderer/       # Classes de renderização e controle de cena
│   │   ├── Main.java       # Ponto de entrada do programa
│   ├── resources/
│       ├── models/         # Modelos OBJ
│       ├── shaders/        # Shaders GLSL
│       ├── scenes/         # Configurações de cena JSON
```

## Dependências

O projeto utiliza as seguintes bibliotecas:

- **LWJGL**: Suporte para OpenGL, GLFW, e Assimp.
- **JOML**: Operações matemáticas 3D.
- **Jackson Databind**: Manipulação de JSON.

Essas dependências estão definidas no arquivo `pom.xml`.

## Autor

Este projeto foi desenvolvido como parte do **Grau B** da disciplina de Computação Gráfica na **Universidade do Vale do Rio dos Sinos**.

## Licença

Este projeto é de uso acadêmico e não possui licença definida.