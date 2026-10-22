# Temperature Converter: Jenkins CI/CD, SCM Trigger and Docker

A complete beginner's guide. Read Part 1 and 2 first, then follow Part 4 step by step.

---

## Part 1: The big picture

**What the app does:** converts Celsius to Fahrenheit, Fahrenheit to Celsius, and Celsius to Kelvin.
**What the project is really about:** automating everything that happens *after* you write code.

Without DevOps, you would do this by hand every time you change the code: compile, test, build a Docker image, run it, upload it. With DevOps, you push code to GitHub and everything happens automatically:

```
You push code to GitHub
        |
        v
Jenkins notices the new commit      (SCM trigger)
        |
        v
Jenkins pulls the code              (Checkout)
        |
        v
Docker builds the image             (Maven compiles and JUnit tests run inside)
        |
        v
Trivy scans the image for security issues
        |
        v
Container runs and prints the result
        |
        v
Image is pushed to Docker Hub
```

If any step fails (for example a test fails), the pipeline stops and shows red. That is **CI/CD**.

---

## Part 2: Every term in plain words

| Term | What it is | Real-life comparison |
|---|---|---|
| **Git** | Tracks changes to your code | Save history for a document |
| **GitHub** | Website that stores your Git repository online | Google Drive for code |
| **Maven** | Java build tool: downloads libraries, compiles, runs tests, makes a `.jar` | A factory manager that follows `pom.xml` |
| **JUnit** | Java testing library | A checklist that verifies your code gives the right answers |
| **JAR file** | Your compiled Java app in one file | A packed lunch box of your program |
| **Jenkins** | Automation server that runs your pipeline | A robot that does your repetitive work |
| **Pipeline / Jenkinsfile** | The list of stages Jenkins runs, written as code | The robot's instruction sheet |
| **CI (Continuous Integration)** | Every code change is automatically built and tested | Spell-check running on every edit |
| **CD (Continuous Delivery/Deployment)** | The tested result is automatically packaged and delivered | Auto-posting the finished product |
| **SCM trigger** | Jenkins starts a build automatically when the repository changes (SCM = Source Code Management, i.e. Git) | A doorbell that wakes the robot |
| **Docker** | Packs your app with everything it needs so it runs the same anywhere | A shipping container |
| **Dockerfile** | Recipe to build the image | Cooking recipe |
| **Image** | The finished, packaged recipe (read-only) | A frozen ready meal |
| **Container** | A running instance of an image | The meal being served |
| **Docker Hub** | Online store for Docker images | App Store for images |
| **Trivy** | Scans an image for known security vulnerabilities | Antivirus for your image |

**SCM trigger, precisely:** in this project Jenkins uses *polling*. Every 2 minutes it asks GitHub "any new commits?" If yes, it builds. The alternative is a *webhook*, where GitHub tells Jenkins instantly. Webhooks need Jenkins to be reachable from the internet, so polling is the easier choice for a college demo.

---

## Part 3: What each file does

| File | Purpose |
|---|---|
| `src/main/java/com/example/TemperatureConverter.java` | The conversion formulas. Rejects values below absolute zero. |
| `src/main/java/com/example/Main.java` | Reads command-line input and prints the result. With no input it prints a demo. |
| `src/test/java/com/example/TemperatureConverterTest.java` | 6 JUnit tests that check the formulas. |
| `pom.xml` | Maven configuration: Java 17, JUnit, jar name, entry point. |
| `Dockerfile` | Two stages. Stage 1 builds and tests with Maven. Stage 2 is a small image that only runs the jar. |
| `Jenkinsfile` | The pipeline: Checkout, Build and Test, Trivy scan, Run, Push. Includes the SCM trigger. |
| `.dockerignore`, `.gitignore` | Files to leave out of Docker builds and Git. |

**Formulas used:** F = C x 9/5 + 32, C = (F - 32) x 5/9, K = C + 273.15

---

## Part 4: Step-by-step setup

### Step 1: Install the tools
1. **Git**: https://git-scm.com
2. **JDK 21**: https://adoptium.net (Temurin 21). Jenkins now needs Java 21 or newer. Your app still targets Java 17 inside Docker, which is fine.
3. **Maven**: https://maven.apache.org (optional, only to run locally; Docker does the real build)
4. **Docker Desktop** (Windows/Mac) or Docker Engine (Linux): https://www.docker.com
5. **Jenkins**: https://www.jenkins.io/download (Windows MSI, or `apt` on Ubuntu)

Check each one in a terminal:
```
git --version
java -version
docker --version
```

### Step 2: Run the app locally first
Always confirm it works on your laptop before automating it.
```
cd temp-converter
mvn clean package
java -jar target/temp-converter.jar
java -jar target/temp-converter.jar c2f 100
```
Expected: the demo output, then `100.00 C = 212.00 F`. `mvn clean package` also runs the 6 tests.

### Step 3: Run it in Docker locally
```
docker build -t temp-converter .
docker run --rm temp-converter
docker run --rm temp-converter c2f 37
```
The first build is slow because Maven downloads libraries. Later builds are faster.

### Step 4: Push the code to GitHub
1. Create a new empty repository on github.com (for example `temp-converter`).
2. In the project folder:
```
git init
git add .
git commit -m "Initial commit: temperature converter"
git branch -M main
git remote add origin https://github.com/<your-username>/temp-converter.git
git push -u origin main
```

### Step 5: Create a Docker Hub account and token
1. Sign up at https://hub.docker.com
2. Account Settings, Security, **New Access Token**. Copy it. It is shown only once.
3. Open `Jenkinsfile` and change `YOUR_DOCKERHUB_USERNAME` to your username. Commit and push.

### Step 6: Set up Jenkins
1. Open http://localhost:8080 and unlock it with the initial admin password (the page tells you where the file is).
2. Choose **Install suggested plugins**, then create an admin user.
3. Make sure Jenkins can use Docker:
   - **Linux:** `sudo usermod -aG docker jenkins` then `sudo systemctl restart jenkins`
   - **Windows:** Docker Desktop must be running. The Jenkinsfile uses `sh`, which is for Linux/Mac. On Windows, replace every `sh` with `bat` and change `$VAR` to `%VAR%`. If this feels messy, running Jenkins on Ubuntu or in WSL is easier.
4. Add Docker Hub credentials: **Manage Jenkins, Credentials, System, Global credentials, Add Credentials**
   - Kind: Username with password
   - Username: your Docker Hub username
   - Password: the access token from Step 5
   - ID: `dockerhub-creds` (must match the Jenkinsfile exactly)

### Step 7: Create the pipeline job
1. **New Item**, name `temp-converter-pipeline`, choose **Pipeline**, OK.
2. Under **Build Triggers**, tick **Poll SCM** and enter `H/2 * * * *`. (The Jenkinsfile already declares this, but ticking it makes the first run pick it up.)
3. Under **Pipeline**, set Definition to **Pipeline script from SCM**.
   - SCM: Git
   - Repository URL: your GitHub repo URL
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
4. Save, then click **Build Now** for the first run.

### Step 8: Prove the SCM trigger works (this is your demo)
1. Edit `Main.java`, for example change the demo title text.
2. ```
   git add .
   git commit -m "Change demo text"
   git push
   ```
3. Do not touch Jenkins. Within about 2 minutes a new build starts by itself. Open the **Console Output** to show the stages running.
4. Show the new image on Docker Hub.

**Bonus demo of a failing pipeline:** change a test so it expects the wrong value, push, and show the build going red at the Build and Test stage. Then fix it and show it going green. This shows exactly why CI exists.

---

## Part 5: Common errors

| Error | Cause and fix |
|---|---|
| `permission denied ... docker.sock` | Jenkins user is not in the docker group. Run the Step 6.3 command and restart Jenkins. |
| `docker: command not found` in Jenkins | Docker is not installed on the Jenkins machine, or Jenkins runs inside a container without Docker. |
| `sh` not recognised / `'sh' is not recognized` | You are on Windows. Use `bat` (see Step 6.3). |
| `credentialsId 'dockerhub-creds' not found` | The credential ID does not match. Recreate it with exactly `dockerhub-creds`. |
| `denied: requested access to the resource is denied` on push | `DOCKERHUB_USER` in the Jenkinsfile is not your real username. |
| Tests fail during the Docker build | Good, this is the pipeline working. Read the test output and fix the code. |
| Trivy stage is slow the first time | It downloads its vulnerability database once. Later runs are faster. |
| Build never triggers | Poll SCM is not set, or the repo URL or branch is wrong. Check **Git Polling Log** in the job page. |

---

## Part 6: Likely viva questions

1. **What is DevOps?** A culture and set of practices that joins development and operations to build, test and release software faster and more reliably, using automation.
2. **What is CI/CD?** CI automatically builds and tests every change. CD automatically delivers the tested result.
3. **What is an SCM trigger?** A trigger that starts a Jenkins build when the source repository changes. Here it is Poll SCM.
4. **Polling vs webhook?** Polling: Jenkins asks the repo on a schedule. Webhook: the repo notifies Jenkins immediately. Webhooks are faster but need Jenkins to be reachable.
5. **Image vs container?** An image is the read-only template. A container is a running instance of it.
6. **Why a multi-stage Dockerfile?** The build stage needs Maven and the JDK, which are large. The final image only needs the JRE, so it is smaller and has less attack surface.
7. **What does `pom.xml` do?** It tells Maven the dependencies, the Java version and how to package the app.
8. **What happens if a unit test fails?** `mvn package` fails, so the Docker build fails, so the pipeline stops before scanning or pushing.
9. **Why Trivy?** To find known vulnerabilities in the image and its base layers before it is shipped. This is DevSecOps.
10. **Declarative vs scripted pipeline?** Ours is declarative: a structured `pipeline { stages { } }` block that is simpler and more readable.
11. **Why not just run it manually?** Manual steps are slow, easy to forget and not repeatable. Automation gives the same result on every commit.

---

## Part 7: Report outline

1. Title and abstract
2. Introduction: what DevOps and CI/CD are
3. Problem statement and objectives
4. Tools used (Java, Maven, JUnit, Git/GitHub, Jenkins, Docker, Trivy, Docker Hub) with one line each
5. System architecture (the flow diagram from Part 1)
6. Implementation: application code, tests, Dockerfile, Jenkinsfile (explain each)
7. Results: screenshots of the Jenkins stage view, console output, a failing build, the SCM-triggered build, Docker Hub image
8. Challenges faced and how you solved them
9. Conclusion and future scope: deploy to Kubernetes, add Slack/email notifications, convert to a REST API, add code-coverage reports
10. References

**Screenshots to take:** Jenkins stage view (all green), console output with the converter result, a red failed build, the automatic build after a push, Docker Hub repository page, Trivy scan output.
