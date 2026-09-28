# Expense Tracker with Spending Insights

A full-stack expense tracker with a Java Spring Boot REST API and a Python FastAPI
microservice for spend forecasting, containerized with Docker Compose.

## Architecture

```
 ┌─────────────┐      HTTP       ┌──────────────────┐      HTTP       ┌───────────────────┐
 │  Frontend   │ ───────────────▶│  Java (Spring     │ ───────────────▶│  Python (FastAPI)  │
 │  (HTML/JS)  │◀─────────────── │  Boot) REST API   │◀─────────────── │  Insights service   │
 └─────────────┘                 └──────────────────┘                 └───────────────────┘
                                          │
                                          ▼
                                   ┌──────────────┐
                                   │  PostgreSQL  │
                                   └──────────────┘
```

- **Java API** — owns expense CRUD (add/edit/delete/view) and persistence in PostgreSQL.
- **Python service** — stateless analytics: category breakdown, monthly trend, and a
  linear-regression forecast of next month's spend (pandas + scikit-learn).
- The Java API calls the Python service over HTTP for the `/api/insights` endpoint —
  separation of concerns, right tool for each job.

## Endpoints

**Java API** (`http://localhost:8080`)
| Method | Path | Description |
|---|---|---|
| GET | `/api/expenses` | List all expenses |
| GET | `/api/expenses/{id}` | Get one expense |
| POST | `/api/expenses` | Add an expense |
| PUT | `/api/expenses/{id}` | Edit an expense |
| DELETE | `/api/expenses/{id}` | Delete an expense |
| GET | `/api/expenses/summary` | Category-wise totals (Java-side) |
| GET | `/api/insights` | Category breakdown + trend + forecast (proxies to Python) |

**Python service** (`http://localhost:8000`)
| Method | Path | Description |
|---|---|---|
| POST | `/analyze` | Takes a list of expenses, returns breakdown/trend/forecast |
| GET | `/health` | Health check |

## Run it

```bash
docker compose up --build
```

Then open `frontend/index.html` in a browser (or serve it with any static server).

- Java API: http://localhost:8080
- Python service: http://localhost:8000/docs (interactive Swagger UI)
- Postgres: localhost:5432

## Run without Docker (local dev)

```bash
# Python service
cd python-service
pip install -r requirements.txt
uvicorn main:app --reload --port 8000

# Java API (needs local Postgres running on 5432, db "expensedb")
cd java-api
mvn spring-boot:run
```

## Sample request/response

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Groceries","amount":1450,"category":"Food","date":"2026-07-05"}'
```

```json
{
  "id": 1,
  "title": "Groceries",
  "amount": 1450.0,
  "category": "Food",
  "date": "2026-07-05"
}
```

```bash
curl http://localhost:8080/api/insights
```

```json
{
  "category_breakdown": {"Food": 8200, "Transport": 3100, "Rent": 15000},
  "monthly_trend": [{"month": "2026-05", "total": 24000}, {"month": "2026-06", "total": 26500}],
  "next_month_forecast": 27800.5
}
```

## Tech stack

Java 17, Spring Boot 3, Spring Data JPA, PostgreSQL, Python 3.11, FastAPI, pandas,
scikit-learn, Docker Compose, HTML/CSS/JS, Chart.js.
