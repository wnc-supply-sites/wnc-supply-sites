# WNC Supply Sites

Live URL: [https://wnc-supply-sites.com]

Staging: [https://staging.wnc-supply-sites.com]

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

- Run the schema migration files found in the 'schema/'
  - The script `./recreate-db.sh` will drop the local test database & run all
    of the migration files. The script can be altered to set up the `wnc_helene`

#### Access local DB
```bash
sudo -u postgres psql
\c wnc_helene
```

- The databases will have empty data. Example data can be found in `src/test/resources/TestData.sql`

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

### Tech Stack Non-Choices

Do not bring these frameworks in, these frameworks are intentionally rejected:
- spring security (if we do integration with a system that uses OAuth2 tokens, like
  FB, or google, then perhaps yes)
- JPA
- guava (just avoid, favor to copy/paste their implementations into a Util class)


### Project Layout

- `webapp/`
  - This is the interesting part that is the webapp.
- `schema/`
  - contains DB migration files

### Env Variables

Configuration values are in 'application.properties'.
The config values all have defaults and will work out of the box.
To override config values, set the appropriate environment variables in IntelliJ,
launch configuration. It will look something like this:
<img width="530" alt="env variables config in IntelliJ"
src="https://github.com/user-attachments/assets/5237ac05-a0f9-4fc0-aaa2-98944364c821">


### Code Organization - DAO's and Controllers

Controller classes are classic spring webserver endpoints. Controller
methods should handle control flow and ideally get actions done
by calling functional private static methods or DAO methods.

Controller's and packages are organized by functionality. 

If a controller is 'tight', pretty small and straight forward,
then the DB access methods might sometimes be directly in the controller.
Otherwise usually DB access code will be in an adjacent "DAO" class.

### Code Formatting

Use google-java-format Intellij plugin.

Formatting can be applied with gradle 'spotless' plugin: `./gradlew spotlessApply`


### Writing unit tests

- all DB queries should be tested in isolation.
- controller logic is ideally tested end-to-end, invoke the controller
  endpoint with a JSON or Map payload and then validate you
  get the right response and/or that the DB changes appropriately
- it's okay to write simple DB queries in the test code to validate
  the system behavior.

### System Authentication

A user is logged in if they have an auth cookie that contains
the correct secret value. There is a RequestInterceptor that
checks a requested URLs prefix and then checks for that cookie
if the URL requires authentication.

The magic cookie value is set by environment variable on startup.
If the cookie value matches, it is valid, otherwise there is
a redirect to the login page.

-----

In the future, it is intended to have individual logins. In which
case the correct cookie value will be a value stored in database.
After a user logs in, we would store a token value in DB,
then we'd check the DB for this token value.

## Deployment

- git push to master
- docker image is automatically built
- ssh to server
- run any psql migrations:
  - `sudo -u postgres psql; \c wnc_helene;`
- run `/root/redeploy.sh`

## Prod Ops

Applications are running in docker containers. 'webapp' is prod, 'staging' is test.
Whether they are connected to airtable is controlled by environment variables.
Staging connects to DB `wnc_helene_test` while webapp connects to the DB `wnc_helene`
All environment variables are in the redeploy sripts in `/root`

### Folders with local git

'git init' was run in the following folders to keep track of configs locally via git.

```
/root
/etc/nginx/
```

### DB Access
```
sudo -u postgres psql
\c wnc_helene
\c wnc_helene_test
```


### Logs

- Tail application logs
```bash
docker logs -f webapp
```

- Tail nginx access logs (can be used to gauge if there are active users in the system)
```bash
sudo tail -f /var/log/nginx/access.log
```

Database logs:
```
tail -200 /var/log/postgresql/postgresql-16-main.log
```


### IP address blocking

If scrapers are putting too much load on the system (or generally doing their scraping thing),
as can be observed from the NGINX access log - they can be blocked by running:

```
sudo /root/block-ip.sh [IP]
```
EG:
```
sudo /root/block-ip.sh 123.10.0.0
```



### DB Updates

Check for the  'schema/' folder
https://github.com/DanVanAtta/wnc-supply-sites/tree/master/schema

Create a new file in there and put the SQL commands in there.

Then run: `./schema/run-flyway.sh`
That will apply all new migrations to your local test database "wnc_helene_test",
and to "wnc_helene"

In production, flyway is automatically run as part of the 'redeploy' scripts.


### How to Rollback:

Do a revert to rollback commits. Push to master. Repeat the deployment
process. Document any rollback SQL updates in a new schema migration
file (`/schem`a` folder)



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
sudo usermod -a -G docker postgres
```

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

