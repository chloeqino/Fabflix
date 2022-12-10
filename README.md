# CS 122B Project 4

## demo video link

https://www.youtube.com/watch?v=4_Pv6ZldYn0

## deploy the project on Tomcat

<ol>
  <li>navigating into the root of project repo where the pom.xml is
  
   
    cd cs122b-fall-team-32
    
  </li>
<li>creating the war file: <br/>

```
mvn package
```

</li>
<li>
copying the newly created war file into the webapps folder under tomcat9:

```
cp cs122b-fall-team-32/target/*.war ./tomcat/webapps/
```

</li>
</ol>

## Contributions of each team member

Kei Asakawa: Implemented task 1, created layout views, and made main and movie list activity

Qinyu Chen: Implemented Single Movie and Login Activity, pagination on mobile,modified the listview,helped with full text search ,and made adapters


