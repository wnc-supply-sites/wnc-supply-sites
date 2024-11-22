# WNC Supply Sites

Live URL: [https://wnc-supply-sites.com]

Webapp for Hurricane Helene disaster relief. The website provides
a search and simple inventory management interface for supply sites.
This allows supply sites to indicate their needs, urgent & surplus
items. Ultimately the goal is to allow for supply items to be redistributed
from sites that have too much, to those that need those items.

## Contributing

- Need system/linux admin
- Pull requests are welcome, open an issue first if you plan to contribute
  so we can coordinate.
- Need help with data entry, supply drivers, dispatchers & communication
  coordinators

## Development 

- postgres installed to bare-metal (laptop)
- Run 'schema1.sql' file and subsequent migration files
  - (data seeding might be difficult. There is a sample data seeding
    config in `TestConfiguration.java`)
- Tests run primarily through IDEA IDE, can be run with gradle as well
- App launched via IDEA IDE, main class is `SuppliesDatabaseApplication.java`

### Tech Stack
 
- springboot
- JDBI
- postgres
- mustache
- vanilla JS
- aspectJ (for testing)
- gradle

### Project Layout

- db-loading
  - This is almost legacy. It's an app to parse a spreadsheet for initial
  data seeding of the database
- webapp
  - This is the interesting part that is the webapp.
- schema
  - contains DB migration files

## Deployment

- git push to master
- docker image is automatically built
- ssh to server
- run any psql migrations:
  - `sudo -u postgres psql; \c wnc_helene;`
- run `/root/redeploy.sh`

## Prod Setup/Install

### System setup

```bash

apt update
apt upgrade
ufw allow 80
ufw allow ssh
ufw enable

apt install nginx -y
apt install postgresql -y
apt install fail2ban -y

curl -fsSL https://get.docker.com/ | sh
useradd webapp
usermod -a -G docker webapp
```

### Create DB

```bash
sudo -u postgres psql
create database wnc_helene;
create user wnc_helene with password '....';
alter database wnc_helene owner to wnc_helene;
```

### DB dump (data seeding)

```
pg_dump -U postgres wnc_helene > db-dump.sql
scp db-dump.sql root@wnc-supply-sites.com

ssh root@wnc-supply-sites.com
sudo -u postgres psql wnc_helene < db-dump.sql
```

### HTTPS config (certbot)

- do nginx reverse proxy config (update sites-enabled/default)
- set server name in nginx config

```bash
apt-get remove certbot
snap install --classic certbot
ln -s /snap/bin/certbot /usr/bin/certbot
certbot --nginx -d wnc-supply-sites.com www.wnc-supply-sites.com
ufw allow 443
```


#### Create cron entry (renew cert)

```bash
root@localhost:/etc/nginx# cat /etc/cron.d/certbot
0 */12 * * * root /usr/bin/certbot -q renew --nginx
```


### Run docker webapp

```bash
export DB_PASS=“.....”
docker run -d --restart always --name webapp --network host -e DB_PASS="$DB_PASS" ghcr.io/danvanatta/wnc-supply-sites/webapp
```

### Redeploy

Create script 'redeploy.sh"
```bash
echo "
docker pull ghcr.io/danvanatta/wnc-helene-supplies-database/webapp:latest
docker stop webapp
docker rm webapp
export DB_PASS=“...."
docker run -d --restart always --name webapp --network host -e AUTH_PASS=... -e AUTH_USER=.... -e DB_PASS=\"\$DB_PASS\" ghcr.io/danvanatta/wnc-helene-supplies-database/webapp
" > redeploy.sh

chmod +x redeploy.sh
```

Then run `redeploy.sh`

