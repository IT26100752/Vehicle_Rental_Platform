# GitHub Setup & Team Workflow — SE1020 Vehicle Rental System

Goal: a team repository whose **commit history proves each member built their
own module** (10 marks + viva evidence). The complete reference implementation
stays on the leader's PC as a *study copy* — the graded repo is built module by
module, one branch per member.

---

## Phase 0 — Every member, once (5 min)

1. Create a GitHub account (use it only for yourself — never share logins).
2. Install **Git for Windows**: https://git-scm.com/download/win (default options).
3. Open *Git Bash* and set YOUR identity (must match your GitHub email so
   commits attach to your avatar):
   ```bash
   git config --global user.name  "Nimal Silva"
   git config --global user.email "nimal@example.com"
   git config --global core.autocrlf true      # Windows line endings, avoids warnings
   git config --global init.defaultBranch main
   ```
4. Check: `git --version` → 2.4x or newer.

---

## Phase 1 — Leader creates the repository (10 min)

1. github.com → **+** (top right) → **New repository**.
2. Name: `se1020-teamXX-vehicle-rental` (your real team number).
   - **Private** (free for teams; add members in the next step).
   - Do **NOT** tick "Add README / .gitignore / license" — we push our own files.
3. Create repository.
4. **Settings → Collaborators and teams → Add people** → invite the other 5
   usernames. Each member accepts the e-mail invite (write access).

> Optional polish: Settings → Branches → protect `main` (require pull-request
> reviews). Nice but not required; a social rule ("nobody commits straight to
> main") is enough for a student team.

---

## Phase 2 — Leader pushes the SKELETON (not the finished app)

The graded repo starts small so that every module arrives later **through a
member's own commits**. On the leader's PC, make a NEW folder next to the
reference copy:

```
D:\oop project\vehicle-rental-system      <- reference / study copy (stays, no git)
D:\oop project\team-repo                  <- the graded repository (new)
```

Copy ONLY these into `team-repo`:

```
pom.xml
.gitignore
README.md
docs/WORKLOAD-DISTRIBUTION.md
docs/MYSQL-SETUP.md
src/main/resources/application.properties
src/main/resources/db/schema.sql
src/main/java/lk/ijse/se1020/vrs/VehicleRentalApplication.java
```

Then in Git Bash inside `team-repo`:

```bash
git init
git add .
git commit -m "chore: project skeleton - build, main class, config, workload plan"
git branch -M main
git remote add origin https://github.com/<LEADER-USERNAME>/se1020-teamXX-vehicle-rental.git
git push -u origin main
```

The first push opens a browser sign-in (Git Credential Manager) — sign in once,
Windows remembers.

---

## Phase 3 — Each member: clone → branch → build → pull request

### 3.1 Clone (every member, own PC)

```bash
cd D:\oop project
git clone https://github.com/<LEADER-USERNAME>/se1020-teamXX-vehicle-rental.git
cd se1020-teamXX-vehicle-rental
```

Open the folder in IntelliJ, set JDK 17, run `VehicleRentalApplication`
(it starts empty — that is expected).

### 3.2 Build order matters (dependencies!)

| Order | Member | Branch name | First PR must contain |
|---|---|---|---|
| 1 | Member 1 | `feature/m1-users` | `Identifiable`, `JsonFileStore`, `AbstractFileRepository`, `CrudService`, `AbstractCrudService`, User hierarchy, `PasswordUtil`, `SessionUtil`, auth pages — **the foundation everyone else compiles against** |
| 2 | Member 2 | `feature/m2-vehicles` | Vehicle hierarchy + repository/service/controller + vehicle pages |
| 3 | Member 3 | `feature/m3-bookings` | Booking + pricing/fine strategies + booking pages |
| 4 | Member 4 | `feature/m4-payments` | Payment + pages |
| 5 | Member 6 | `feature/m6-reviews` | Review + moderation pages |
| 6 | Member 5 | `feature/m5-admin` | dashboard, interceptor, admin pages (needs the others merged) |

Members 2–6 start their branch **after** Member 1's PR is merged:

```bash
git checkout main
git pull                      # get the merged foundation
git checkout -b feature/m2-vehicles
```

### 3.3 Daily loop inside your branch

```bash
# code in IntelliJ ... then:
git status                          # see what changed
git add src/main/java/lk/ijse/se1020/vrs/model/Vehicle.java   # add YOUR files
git commit -m "vehicles: add Vehicle hierarchy with four subclasses"
git push -u origin feature/m2-vehicles        # first push of the branch
git push                                      # later pushes
```

Commit after every meaningful step (entity → repository → service → controller
→ templates → fix). 10–20 small commits per member is exactly the evidence the
marking guide wants. Message style: `module: what changed`.

### 3.4 Pull request

1. On github.com the repo shows "Compare & pull request" for your new branch —
   click it (or *Pull requests → New pull request*, base `main` ← your branch).
2. Title: `Member 2 - Vehicle Management (CRUD + pages)`.
3. Assign one teammate as reviewer; when approved (or after the team's
   agreement) click **Merge pull request** → *Create a merge commit*
   (keeps every individual commit visible — do NOT squash).
4. Delete the branch on GitHub; locally: `git checkout main && git pull && git branch -d feature/m2-vehicles`.

### 3.5 Ownership rule (prevents all merge conflicts)

Touch only the files listed for you in `docs/WORKLOAD-DISTRIBUTION.md`.
Shared files (`fragments/layout.html`, `config/*`) belong to Member 5 — ask
them to add navbar links for your module inside their PR.

---

## Phase 4 — Evidence for the report & viva

```bash
git log --graph --oneline --all > docs/git-history.txt
```

Plus screenshots of: GitHub → Insights → **Contributors** (six coloured bars),
the closed **Pull requests** list, and one member's commit list. Paste all
three into the report's "Git repository commit history" section.

At the viva, open **your own** commits/PR and walk the examiner through one
file end to end.

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| "remote origin already exists" after `git remote add` | `git remote set-url origin <url>` |
| Login window keeps appearing | Sign in via the browser button in Git Credential Manager, not by typing a password (GitHub no longer accepts account passwords for git) |
| `target/` or `.idea/` appeared in `git status` | your `.gitignore` is missing — copy it from the reference folder, then `git rm -r --cached target .idea` |
| Merge conflict anyway | open the file, keep both sides' code between `<<<<<<<` / `>>>>>>>`, `git add`, `git commit` |
| Forgot to pull before starting | `git checkout main && git pull` then `git checkout -b feature/...` again |
| Committed with the wrong e-mail | amend the last commit: `git commit --amend --reset-author` (before pushing) |
