from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
from datetime import date
import pandas as pd
from sklearn.linear_model import LinearRegression
import numpy as np

app = FastAPI(title="Expense Insights Service")


class ExpenseRecord(BaseModel):
    id: Optional[int] = None
    title: Optional[str] = None
    amount: float
    category: str
    date: date


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze")
def analyze(expenses: List[ExpenseRecord]):
    if not expenses:
        return {
            "category_breakdown": {},
            "monthly_trend": [],
            "next_month_forecast": 0
        }

    df = pd.DataFrame([e.dict() for e in expenses])
    df["date"] = pd.to_datetime(df["date"])
    df["month"] = df["date"].dt.to_period("M")

    # Category breakdown
    category_breakdown = df.groupby("category")["amount"].sum().round(2).to_dict()

    # Monthly trend
    monthly = df.groupby("month")["amount"].sum().sort_index()
    monthly_trend = [
        {"month": str(m), "total": round(v, 2)} for m, v in monthly.items()
    ]

    # Simple forecast: linear regression on month index -> total spend
    forecast = _forecast_next_month(monthly)

    return {
        "category_breakdown": category_breakdown,
        "monthly_trend": monthly_trend,
        "next_month_forecast": forecast
    }


def _forecast_next_month(monthly: pd.Series) -> float:
    if len(monthly) < 2:
        # not enough history to fit a trend line, just repeat the last known total
        return round(float(monthly.iloc[-1]), 2) if len(monthly) == 1 else 0.0

    X = np.arange(len(monthly)).reshape(-1, 1)
    y = monthly.values

    model = LinearRegression()
    model.fit(X, y)

    next_index = np.array([[len(monthly)]])
    prediction = model.predict(next_index)[0]

    return round(max(float(prediction), 0.0), 2)
