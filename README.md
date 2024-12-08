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

### Local Setup

- install postgres to your machine (bare-metal)
   - ideally this would be a postgres on docker. If you get that setup, please update these README instructions
- Create databases. Two of them. Login and user are same for both. One is 'wnc_helene' which will be used 
  when running the app locally and accessing via "localhost:8080", the other DB, 'wnc_helene_test' is used 
  by unit tests.

```bash
sudo -u postgres psql
create database wnc_helene;
create user wnc_helene with password 'wnc_helene';
alter database wnc_helene owner to wnc_helene;

create database wnc_helene_test;
create user wnc_helene_test with password 'wnc_helene';
alter database wnc_helene_test owner to wnc_helene;
```

- Run the schema migration files found in the 'schema/' folder (schema1.sql and on, in order)
   - ideally this would be executed via flyway
   - needs to be done for both database

```bash
sudo -u postgres psql
\c wnc_helene
# [now copy paste contents of each schema file]
```

- The databases will have empty data. Example data can be found in `TestConfiguration.java`

- Finally, the app can be launched via Intellj IDE, main class is `SuppliesDatabaseApplication.java`
  - The app can also be likely be run via gradle `./gradlew bootRun` (not well tested/vetted, but should work)

- A few environment variables need to be set. This can be done in the run config in IntelliJ:
  - WEBHOOK_SECRET  (can be set to any value)

### Development - Running unit tests

- Tests run primarily through IntelliJ IDE, right click 'test' folder & run
- Test can be run with gradle as well `./gradlew test`


### Tech Stack
 
- springboot
- JDBI
- postgres
- mustache
- vanilla JS
- aspectJ (for testing)
- gradle

### Project Layout

- webapp
  - This is the interesting part that is the webapp.
- schema
  - contains DB migration files

### Env Variables

Configuration values are in 'application.properties'.
The config values all have defaults and will work out of the box.
To override config values, set the appropriate environment variables in IntelliJ,
launch configuration. It will look something like this:
<img width="530" alt="env variables config in IntelliJ"
src="https://github.com/user-attachments/assets/5237ac05-a0f9-4fc0-aaa2-98944364c821">



## Deployment

- git push to master
- docker image is automatically built
- ssh to server
- run any psql migrations:
  - `sudo -u postgres psql; \c wnc_helene;`
- run `/root/redeploy.sh`

## Prod Ops

### Logs

- Tail application logs
```bash
docker logs -f webapp
```

- Tail nginx access logs (can be used to gauge if there are active users in the system)
```bash
sudo tail -f /var/log/nginx/access.log
```

### Linux User setup: Adding SSH user with sudo

* Requires a public SSH key
* Requires pre-existing access to the server
* Access granted provides full sudo

```bash
# Set these two variables:

USERNAME=[last name of user here, all lower case, no spaces]
PUBLIC_SSH_KEY="[add public key here, do use double quotes around it]"


# Now run the following, (can copy paste and run everything below):

## Create user with home folder
sudo useradd $USERNAME
sudo chsh -s /usr/bin/bash $USERNAME
sudo mkdir -p /home/$USERNAME/.ssh/

## Set up the SSH key for access
sudo chmod 0700 /home/$USERNAME/.ssh/ 
echo "$PUBLIC_SSH_KEY" | sudo tee /home/$USERNAME/.ssh/authorized_keys
sudo chmod 0600 /home/$USERNAME/.ssh/authorized_keys
sudo chown -R $USERNAME:$USERNAME /home/$USERNAME

## Enable sudo for the user
sudo cp /root/template-sudoers-file /root/$USERNAME
sudo sed -i "s/^USERNAME/$USERNAME/" /root/$USERNAME
sudo mv -v /root/$USERNAME /etc/sudoers.d/


# Add user to 'docker' group, allows for 'docker' commands
# to be run without sudo
usermod -a -G docker $USERNAME
```



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

create database wnc_helene_test;
alter database wnc_helene_test owner to wnc_helene;
```



### DB dump (data seeding)

```
pg_dump -U postgres wnc_helene > db-dump.sql
scp db-dump.sql root@wnc-supply-sites.com

ssh root@wnc-supply-sites.com
sudo -u postgres psql wnc_helene < db-dump.sql
```

DB dump from prod to staging:
```
# todo: likely need to recreate test database
sudo -u postgres pg_dump -U postgres wnc_helene > db-dump.sql
sudo -u postgres psql wnc_helene_test < db-dump.sql
rm db-dump.sql
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

*Configure Staging*:

```bash
certbot --nginx -d staging.wnc-supply-sites.com
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

