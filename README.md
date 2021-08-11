# Bootcamp Orange Talents 

Conteúdo: https://github.com/zup-academy/nosso-cartao-documentacao/tree/master/orange-talent-4

## Desafio Transação  

Implementar um sistema distribuído que simule o funcionamento do PIX

Requisitos: https://github.com/zup-academy/orange-stack-documentacao/tree/master/desafio-01/01-key-manager#desafio-pix  

## Tecnologias utilizadas:
- Kotlin
- Micronaut
- gRPC
- Kubernetes
- Feign
- Postgresql
- Mockito
- Hamcrast
- Junit
- H2


### Funcionalidades implementadas:  

[X] - 001-setup-do-projeto-key-manager-grpc  
[X] - 005-registrando-uma-nova-chave-pix  
[X] - 006-testando-registro-de-chave-pix  
[X] - 010-removendo-uma-chave-pix-existente  
[X] - 011-testando-remocao-de-chave-pix-existente  
[X] - 015-registrando-e-excluindo-chaves-pix-no-bcb  
[X] - 016-testando-registro-e-exclusao-de-chaves-pix-no-bcb  
[X] - 020-consultando-os-dados-de-uma-chave-pix  
[X] - 021-listando-todas-as-chaves-pix-do-cliente  
[X] - 021-testando-consulta-de-dados-de-uma-chave-pix  
[X] - 023-testando-listagem-de-chaves-pix   
[X] - 050-deployment-dos-servicos-dockerfile  
[X] - 055-deployment-dos-servicos-manifestos-k8s  
[X] - 060-deployment-dos-servicos-codebuild  
[X] - 065-deployment-dos-servicos-validando 

### API HTTP Client

- https://github.com/priscilasanfer/orange-talents-04-template-pix-keymanager-rest


### Rodar a Aplicação

- Rodar sem o docker:  
  - .\gradlew build  
  - java -jar .\build\libs\key-manager-rest-0.1-all.jar  

- Rodar com o docker:  
  - docker-compose up -d  


### Curso Zup Academy sobre Deploy

- [Codebuild: O que é?](https://youtu.be/ESetAFRhn5M)
- [Codebuild: Buildspec.yaml](https://youtu.be/8JXAwykjp-Q)
- [Codebuild: Criando projeto](https://youtu.be/IswwVdJREHI)
- [Codebuild: Explicando a criação do projeto](https://youtu.be/iTkDmOhxIvo)
- [Codedeploy: O que é?](https://youtu.be/8AXCIi_Wryo)
- [Codedeploy: App Spec](https://youtu.be/lvBxrlSk8fc)
- [Codedeploy: Criando explicação](https://youtu.be/6iuLxEqOeis)
- [Codedeploy: Explicando a criação](https://youtu.be/fjgLeINXctI)
- [Codedeploy: Custo](https://youtu.be/fjgLeINXctI )
- [Codepipeline: O que é?](https://youtu.be/YWXsdLvn-lg)
- [ECR: O que é?](https://youtu.be/J9RCWLFbd2Q)
- [ECR: Navegando na interface](https://youtu.be/ekJ6Wn8pMEY)
- [ECR: Explicando e conhecendo a interface](https://youtu.be/wy0KiEc1t8g)
- [Criando um Pipeline - Apresentação projeto](https://youtu.be/C3TAITn8f8k)
- [Criando um Pipeline - O código utilizado](https://youtu.be/HTpPYAx4fBM)
- [Criando um Pipeline - Explicando o código utilizado](https://youtu.be/HL3n5at_hoU)
- [Criando um Pipeline - Roles](https://youtu.be/d5L7UiI6ufk)
- [Criando um Pipeline - Explicação das roles](https://youtu.be/Vat5ZsW7vpM)
- [Criando um Pipeline - EC2](https://youtu.be/5I3vN1aVKOo)
- [Criando um Pipeline - Explicação EC2](https://youtu.be/AujNP1aa-Q0)
- [Criando um Pipeline - Codebuild](https://youtu.be/nlBcZ9gzpXw)
- [Criando um Pipeline - Explicação Codebuild](https://youtu.be/LwQlkX0QZMU)
- [Criando um Pipeline - Codedeploy](https://youtu.be/ka1wflga_zA)
- [Criando um Pipeline - Explicação Codedeploy](https://youtu.be/WQ3ODnh_cOA)
- [Criando um Pipeline - Codepipeline](https://youtu.be/v-2abHknrPo)
- [Criando um Pipeline - Explicação Codepipeline](https://youtu.be/3uEzsa0llWo)
- [Apresentando Helm](https://youtu.be/6uoUNcM_JoY)
- [Explicação sobre o exemplo já implementado](https://youtu.be/9raXC_eTec8)
- [Explicação enquanto implementa o exemplo](https://youtu.be/UfCPMRV9J-c)


### Comandos Kubernetes

- create keymanager-grpc    
- helm repo add stable https://charts.helm.sh/stable  
- helm repo list  
- helm repo update  
- helm install weave stable/weave-scope (visibilidade do cluster)   
- helm delete weave  
- kubectl create ns dev  
- helm install keymanager-grpc -n dev --values keymanager-grpc\values.yaml .\keymanager-grpc  
- helm uninstall keymanager-grpc  
- kubectl get pod -n dev  
- kubectl describe pod POD_NAME -n dev  

Mais detalhes: https://www.youtube.com/watch?v=UfCPMRV9J-c   
  

### Habilitar log no client

No arquivo ```logback.xml``` adicionar: 
```
<logger name="io.micronaut.http.client" level="TRACE"/>
```


