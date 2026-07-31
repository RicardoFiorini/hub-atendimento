# Guia de Deploy — VPS Hostinger (Ubuntu + Apache já em uso)

Este guia assume: Apache já rodando na porta 80/443 servindo seu site atual, MySQL já instalado, e você quer subir o Hub em `hub.ricardofiorini.com` sem derrubar nada existente.

## 0. Antes de tudo: DNS

No painel da Hostinger (ou onde seu domínio está registrado), crie um registro:
```
Tipo: A
Nome: hub
Valor: <IP da sua VPS>
TTL: padrão
```
Propagação pode levar de alguns minutos a algumas horas.

## 1. Instalar Java 21

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
java -version   # confirma "21"
```

## 2. Criar banco e usuário MySQL dedicados ao projeto

Não reaproveite o usuário root do MySQL — crie um usuário isolado para o Hub:

```bash
sudo mysql -u root -p
```
```sql
CREATE DATABASE hub_atendimento CHARACTER SET utf8mb4;
CREATE USER 'hub_user'@'localhost' IDENTIFIED BY 'ESCOLHA_UMA_SENHA_FORTE_AQUI';
GRANT ALL PRIVILEGES ON hub_atendimento.* TO 'hub_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

## 3. Build do backend

Na sua máquina local (Windows), usando o Maven Wrapper que já vem no projeto — não precisa instalar Maven:
```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
# gera backend/target/hub-atendimento-1.0.0.jar
```
Na primeira execução, o `mvnw.cmd` baixa o Maven sozinho (só precisa de internet e do Java instalado). Isso pode levar 1-2 minutos.

Envie o `.jar` pra VPS:
```bash
scp target/hub-atendimento-1.0.0.jar usuario@SEU_IP:/opt/hub-atendimento/
```

## 4. Arquivo de variáveis de ambiente

Na VPS, crie `/opt/hub-atendimento/.env`:
```bash
sudo mkdir -p /opt/hub-atendimento
sudo nano /opt/hub-atendimento/hub.env
```
Conteúdo:
```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=hub_atendimento
DB_USER=hub_user
DB_PASSWORD=ESCOLHA_UMA_SENHA_FORTE_AQUI
JWT_SECRET=troque-por-uma-string-aleatoria-de-pelo-menos-32-caracteres
CORS_ORIGIN=https://hub.ricardofiorini.com
```

## 5. Rodar o backend como serviço (systemd)

```bash
sudo nano /etc/systemd/system/hub-backend.service
```
```ini
[Unit]
Description=Hub de Atendimento - Backend Spring Boot
After=network.target mysql.service

[Service]
Type=simple
User=www-data
EnvironmentFile=/opt/hub-atendimento/hub.env
ExecStart=/usr/bin/java -jar /opt/hub-atendimento/hub-atendimento-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Ativar:
```bash
sudo systemctl daemon-reload
sudo systemctl enable hub-backend
sudo systemctl start hub-backend
sudo systemctl status hub-backend   # confirma "active (running)"
```

Ver logs se algo der errado:
```bash
sudo journalctl -u hub-backend -f
```

O backend sobe na porta **8081** (interna, não exposta direto à internet).

## 6. Build do frontend

Local ou na VPS:
```bash
cd frontend
npm install
npm run build
# gera frontend/dist/hub-atendimento-frontend/
```

Envie os arquivos estáticos:
```bash
scp -r dist/hub-atendimento-frontend usuario@SEU_IP:/var/www/hub-frontend
```

## 7. Configurar Apache: novo VirtualHost pro subdomínio + proxy reverso pra API

Habilitar módulos necessários (uma vez só):
```bash
sudo a2enmod proxy proxy_http rewrite ssl
sudo systemctl restart apache2
```

Criar o VirtualHost:
```bash
sudo nano /etc/apache2/sites-available/hub.ricardofiorini.com.conf
```
```apache
<VirtualHost *:80>
    ServerName hub.ricardofiorini.com

    DocumentRoot /var/www/hub-frontend

    <Directory /var/www/hub-frontend>
        Options -Indexes +FollowSymLinks
        AllowOverride All
        Require all granted
        # Angular usa roteamento client-side: redireciona tudo pro index.html
        FallbackResource /index.html
    </Directory>

    # Proxy reverso: tudo que chegar em /api vai pro Spring Boot na porta 8081
    ProxyPreserveHost On
    ProxyPass /api http://localhost:8081/api
    ProxyPassReverse /api http://localhost:8081/api

    ErrorLog ${APACHE_LOG_DIR}/hub-error.log
    CustomLog ${APACHE_LOG_DIR}/hub-access.log combined
</VirtualHost>
```

Ativar o site:
```bash
sudo a2ensite hub.ricardofiorini.com.conf
sudo systemctl reload apache2
```

Seu site principal continua intacto, servido pelo VirtualHost dele — este é um arquivo novo e separado.

## 8. HTTPS gratuito (Let's Encrypt)

```bash
sudo apt install -y certbot python3-certbot-apache
sudo certbot --apache -d hub.ricardofiorini.com
```
O certbot detecta o VirtualHost criado e configura o HTTPS automaticamente, criando a versão `:443` do arquivo de config.

## 9. Checklist final

- [ ] DNS do subdomínio `hub.ricardofiorini.com` apontando pra VPS
- [ ] `systemctl status hub-backend` → `active (running)`
- [ ] `curl http://localhost:8081/actuator/health` → responde (se adicionar o actuator) ou teste `/api/auth/login` com o admin seed
- [ ] Acessar `https://hub.ricardofiorini.com` no navegador → tela de login aparece
- [ ] Login com `admin@hub.local` / `admin123` funciona
- [ ] Seu site principal (raiz do domínio) continua no ar normalmente

## Troubleshooting rápido

- **502 Bad Gateway no `/api`**: backend não subiu — `sudo journalctl -u hub-backend -f`
- **Tela branca no Angular**: falta o `FallbackResource /index.html` no VirtualHost (rotas do Angular Router precisam cair sempre no index.html)
- **CORS error no console**: confira se `CORS_ORIGIN` no `hub.env` bate exatamente com a URL usada no navegador (com `https://`, sem barra no final)
