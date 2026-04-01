# Aplicação do SOLID (SRP)

Este é o repositório do meu trabalho da UCSAL aplicando os conceitos de SOLID no nosso projeto em Java. 

Nesta primeira etapa, o foco foi aplicar o "S" do SOLID, que é o Princípio da Responsabilidade Única (SRP).

## O que estava ruim no código inicial?
Na primeira versão, a classe `App.java` estava fazendo de tudo um pouco. Ela era responsável por exibir o menu na tela, gerenciar as listas de dados (participantes, provas, etc.) e ainda calcular a nota final das provas. Isso deixava a classe gigante e com várias responsabilidades misturadas, o que dificultava na hora de mexer no código.

## Como eu resolvi:
Para organizar tudo e respeitar o SRP, eu dividi o código para que cada classe tivesse apenas um único trabalho:

1. Repositórios: Criei classes separadas só para guardar e buscar os dados na memória (como se fosse um banco de dados). Agora temos o `ParticipanteRepository`, `ProvaRepository`, `QuestaoRepository` e `TentativaRepository`.
2. Serviço de Avaliação: Tirei a lógica de calcular os acertos de dentro do `App` e criei a classe `AvaliacaoService`. O único trabalho dela é receber uma tentativa e devolver a nota calculada.
3. O novo App.java: Agora a classe principal ficou super limpa. A única responsabilidade dela é interagir com o usuário (mostrar o menu e ler o que é digitado no teclado). Ela apenas aciona os repositórios e o serviço quando precisa de algo.

Com isso, cada classe passou a ter um único motivo para mudar, deixando o projeto mais organizado e muito mais fácil de entender.

## Princípio do Aberto/Fechado (OCP)
O problema: O método que calculava a nota da prova estava fixo. Se o precisasse mudar a regra (ex: questões com pesos diferentes), eu teria que alterar o código que já estava funcionando.
A solução: Criei uma interface `CalculadoraNota` e uma implementação `CalculadoraNotaPadrao`. Agora, se surgir uma nova regra de pontuação, eu só preciso criar uma nova classe que implementa essa interface, sem mexer no que já está pronto. O código ficou fechado para modificação, mas aberto para extensão.