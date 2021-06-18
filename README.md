# Por favor faça um Fork desse projeto!

## Está em dúvida de como fazer um Fork? Não tem problema! [Aqui tem uma explicação do que entendemos que você deve considerar!](https://docs.github.com/en/github/getting-started-with-github/fork-a-repo)


Rodar sem o docker:  
- .\gradlew build  
- java -jar .\build\libs\key-manager-rest-0.1-all.jar  

rodar com docker:  
- docker-compose up -d  

criar o helm:  
helm create keymanager-grpc    

comandos: https://www.youtube.com/watch?v=UfCPMRV9J-c    

- helm repo add stable https://charts.helm.sh/stable  
  helm repo list  
  helm repo update  
- helm install weave stable/weave-scope (visibilidade do cluster)   
  helm delete weave  
- kubectl create ns dev  
- helm install keymanager-grpc -n dev --values keymanager-grpc\values.yaml .\keymanager-grpc  
  helm uninstall keymanager-grpc  
  kubectl get pod -n dev  
  kubectl describe pod POD_NAME -n dev  