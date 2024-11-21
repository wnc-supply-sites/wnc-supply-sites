# webapp

Web front end for database.
Provides simple capability to filter for item, site, county.


## Prod Setup/Install

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


### Create DB

sudo -u postgres psql
create database wnc_helene;
create user wnc_helene with password '....';
alter database wnc_helene owner to wnc_helene;



### DB dump

pg_dump -U postgres wnc_helene > db-dump.sql
scp db-dump.sql root@wnc-supplies-database.org

ssh root@wnc-supplies-database.org
sudo -u postgres psql wnc_helene < db-dump.sql


### HTTPS config

- install nginx via apt
- do nginx reverse proxy config (update sites-enabled/default)
- set server name in nginx config
-
apt-get remove certbot
snap install --classic certbot
ln -s /snap/bin/certbot /usr/bin/certbot
certbot --nginx
certbot --expand -d wnc-supplies.com -d www.wnc-supplies.com
ufw allow 443


#### Create cron entry
root@localhost:/etc/nginx# cat /etc/cron.d/certbot
0 */12 * * * root /usr/bin/certbot -q renew --nginx


### Run docker webapp


export DB_PASS=“.....”
docker run -d --restart always --name webapp --network host -e DB_PASS="$DB_PASS" ghcr.io/danvanatta/wnc-helene-supplies-database/webapp

### Redeploy

Create script 'redeploy.sh"

echo "
docker pull ghcr.io/danvanatta/wnc-helene-supplies-database/webapp:latest
docker stop webapp
docker rm webapp
export DB_PASS=“...."
docker run -d --restart always --name webapp --network host -e AUTH_PASS=... -e AUTH_USER=.... -e DB_PASS=\"\$DB_PASS\" ghcr.io/danvanatta/wnc-helene-supplies-database/webapp
" > redeploy.sh

chmod +x redeploy.sh


