## JPA sample code

### h2 server container
```
docker run -d -p 1521:1521 -p 8081:81 -v /Users/user/Desktop/geonyeongkim/db/h2:/opt/h2-data -e H2_OPTIONS='-ifNotExists' --name=h2 oscarfonts/h2
```
