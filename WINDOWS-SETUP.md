# Windows Setup: Temperature Converter with Jenkins, SCM Trigger and Docker

Follow the steps in order. Do not skip ahead. After each step there is a "Check" so you know it worked.
Open **PowerShell** for the commands (Start menu, type "PowerShell").

---

## Step 0: Check virtualization (Docker needs it)
1. Press Ctrl+Shift+Esc to open Task Manager, go to **Performance**, then **CPU**.
2. Look at the bottom right for **Virtualization: Enabled**.
3. If it says Disabled, you must enable it in BIOS (search "enable virtualization <your laptop brand>").

## Step 1: Install Git
1. Download from https://git-scm.com/download/win and install with all default options.
2. Close and reopen PowerShell.
3. **Check:** `git --version`

## Step 2: Install Java 21
Jenkins needs Java 21 or newer (Java 17 no longer works with current Jenkins).
1. Go to https://adoptium.net, download **Temurin 21 (LTS), Windows x64, .msi**.
2. In the installer, on the "Custom Setup" screen, set **"Add to PATH"** and **"Set JAVA_HOME variable"** to be installed (click the drive icon, choose "Will be installed on local hard drive").
3. Reopen PowerShell.
4. **Check:** `java -version` (should show 21.x)

You do NOT need to install Maven. Docker will run Maven for you inside the build.

## Step 3: Install Docker Desktop
1. Download from https://www.docker.com/products/docker-desktop and install. Keep "Use WSL 2" ticked.
2. Restart the PC when asked, then open **Docker Desktop** and wait until it says "Engine running" (bottom left, green).
3. **Check:** `docker run hello-world` (prints "Hello from Docker!")
- If you see "WSL 2 installation is incomplete", run `wsl --update` in PowerShell as Administrator and restart.

## Step 4: Run the project in Docker (before any Jenkins)
1. Unzip `temp-converter.zip`, for example to `C:\projects\temp-converter`.
2. ```
   cd C:\projects\temp-converter
   docker build -t temp-converter .
   docker run --rm temp-converter
   docker run --rm temp-converter c2f 100
   docker run --rm temp-converter f2c 98.6
   ```
3. **Check:** you see the demo output, then `100.00 C = 212.00 F`.
The first build takes a few minutes because Maven downloads libraries.
If the build fails, send me the last 30 lines of the output.

## Step 5: Put the code on GitHub
1. Create an account at https://github.com and click **New repository**. Name it `temp-converter`, make it **Public** (this avoids login problems in Jenkins), do not add a README, then Create.
2. In PowerShell inside the project folder:
```
git config --global user.name "Your Name"
git config --global user.email "your-github-email@example.com"
git init
git add .
git commit -m "Initial commit: temperature converter"
git branch -M main
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/temp-converter.git
git push -u origin main
```
3. A browser window opens to log in to GitHub. Approve it.
4. **Check:** refresh the GitHub page and see your files.

## Step 6: Docker Hub account and token
1. Sign up at https://hub.docker.com.
2. Click your profile, **Account settings**, **Personal access tokens**, **Generate new token**. Give it Read and Write access. **Copy it now**, because it is shown only once.
3. Open `Jenkinsfile` in Notepad and replace `YOUR_DOCKERHUB_USERNAME` with your Docker Hub username.
4. Save it, then push:
```
git add .
git commit -m "Set Docker Hub username"
git push
```

## Step 7: Run Jenkins
The easiest and most reliable way on Windows for a college project is to run Jenkins as yourself from a terminal. (The Windows installer runs Jenkins as a system account that often cannot talk to Docker Desktop, which causes confusing errors.)

1. Go to https://www.jenkins.io/download and download the **Generic Java package (.war)** under LTS. Save it to `C:\jenkins\jenkins.war`.
2. In PowerShell:
```
cd C:\jenkins
java -jar jenkins.war --httpPort=8080
```
3. Wait until you see "Jenkins is fully up and running". Near the top of the output there is a long **initial admin password**. Copy it.
4. Open http://localhost:8080 in your browser, paste the password, choose **Install suggested plugins**, create an admin user, and keep the default URL.
5. **Leave this PowerShell window open.** Closing it stops Jenkins. Next time, repeat from step 2 only.
- If port 8080 is busy, use `--httpPort=9090` and open http://localhost:9090.
- Docker Desktop must be running whenever Jenkins runs.

## Step 8: Add your Docker Hub credentials in Jenkins
1. **Manage Jenkins**, **Credentials**, **System**, **Global credentials (unrestricted)**, **Add Credentials**.
2. Kind: **Username with password**
3. Username: your Docker Hub username
4. Password: the access token from Step 6
5. ID: `dockerhub-creds` (exactly this)
6. Create.

## Step 9: Create the pipeline job
1. **New Item**, name `temp-converter-pipeline`, choose **Pipeline**, OK.
2. Under **Build Triggers**, tick **Poll SCM** and enter `H/2 * * * *`.
3. Under **Pipeline**:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/YOUR_GITHUB_USERNAME/temp-converter.git`
   - Branch Specifier: `*/main`
   - Script Path: `Jenkinsfile`
4. **Save**, then click **Build Now**.
5. Click the build number (#1), then **Console Output**.
6. **Check:** the end says `Finished: SUCCESS`, and the **Stage View** shows green boxes. Your image should appear on Docker Hub.
The first run is slow (downloads the Trivy database too). Later runs are faster.

## Step 10: Demonstrate the SCM trigger
1. Edit `src\main\java\com\example\Main.java`, change the text "Temperature Converter Demo" to something else.
2. ```
   git add .
   git commit -m "Change demo title"
   git push
   ```
3. Do not click anything in Jenkins. Within about 2 minutes build #2 starts by itself. Open its Console Output to show the new title printed by the container.

**Failing build demo:** in `TemperatureConverterTest.java` change `32.0` to `33.0` in the first test, push, and show the build turn red at the Build and Test stage. Change it back, push, and show it turn green.

---

## Windows-specific errors

| Error | Fix |
|---|---|
| `'docker' is not recognized` in Jenkins | Install Docker Desktop, then stop Jenkins (Ctrl+C), open a NEW PowerShell, and start Jenkins again so it sees the new PATH. |
| `error during connect ... dockerDesktopLinuxEngine ... cannot find the file` | Docker Desktop is not running. Start it and wait for "Engine running". |
| `'git' is not recognized` | Install Git (Step 1), then restart Jenkins from a new PowerShell. |
| `docker: Error response ... Windows containers` | Right-click the Docker tray icon and choose **Switch to Linux containers**. |
| Stage 'Security Scan' is yellow | Trivy had a problem (often network). The pipeline continues. Rerun, or tell me the error shown in the console. |
| `denied: requested access to the resource is denied` | The Docker Hub username in the Jenkinsfile is wrong. |
| `credentialsId 'dockerhub-creds' not found` | The credential ID does not match exactly. |
| Build never triggers on push | Poll SCM not ticked, or wrong repo URL or branch. Open the job, then **Git Polling Log**. |
| Jenkins says "Please wait while Jenkins is getting ready" for a long time | Normal on first start. Wait 1 to 2 minutes. |
