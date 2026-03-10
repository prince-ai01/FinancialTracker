"""
Financial Tracker - AI Spending Pattern Analysis
2nd Year, 4th Semester Project
Uses: pandas, matplotlib, sklearn (Linear Regression)
"""

import mysql.connector
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib import gridspec
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import LabelEncoder
import numpy as np
from datetime import datetime
import warnings
warnings.filterwarnings("ignore")

# ─── Database Connection ─────────────────────────────────────────
def get_data():
    try:
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="your_password",
            database="financial_tracker"
        )
        query = """
            SELECT t.transaction_date, t.type, t.amount, c.category_name
            FROM transactions t
            JOIN categories c ON t.category_id = c.category_id
            WHERE t.user_id = 1
            ORDER BY t.transaction_date
        """
        df = pd.read_sql(query, conn)
        conn.close()
        return df
    except Exception as e:
        print(f"[DB Error] {e}")
        # Return sample data if DB not connected
        return pd.DataFrame({
            'transaction_date': pd.date_range('2025-01-01', periods=14, freq='7D'),
            'type': ['income','expense','expense','expense','expense',
                     'income','expense','expense','expense','expense',
                     'income','expense','expense','expense'],
            'amount': [15000,2500,800,3000,1200,
                       15000,2200,500,800,5000,
                       15000,2800,1500,2000],
            'category_name': ['Salary','Food','Transport','Shopping','Bills',
                               'Salary','Food','Entertainment','Transport','Education',
                               'Salary','Food','Health','Shopping']
        })

# ─── Analysis Functions ──────────────────────────────────────────
def spending_pattern_analysis(df):
    df['transaction_date'] = pd.to_datetime(df['transaction_date'])
    df['month'] = df['transaction_date'].dt.to_period('M').astype(str)
    df['month_num'] = df['transaction_date'].dt.month

    expenses = df[df['type'] == 'expense'].copy()
    income   = df[df['type'] == 'income'].copy()

    print("=" * 55)
    print("   📊 FINANCIAL TRACKER - AI SPENDING ANALYSIS")
    print("=" * 55)

    # 1. Monthly Summary
    monthly_expense = expenses.groupby('month')['amount'].sum()
    monthly_income  = income.groupby('month')['amount'].sum()
    print("\n📅 Monthly Summary:")
    for m in sorted(set(monthly_expense.index) | set(monthly_income.index)):
        inc = monthly_income.get(m, 0)
        exp = monthly_expense.get(m, 0)
        bal = inc - exp
        status = "✅" if bal >= 0 else "⚠️"
        print(f"  {m}: Income ₹{inc:.0f} | Expense ₹{exp:.0f} | Balance ₹{bal:.0f} {status}")

    # 2. Category-wise Spending
    print("\n📂 Category-wise Spending:")
    cat_summary = expenses.groupby('category_name')['amount'].sum().sort_values(ascending=False)
    total_exp = cat_summary.sum()
    for cat, amt in cat_summary.items():
        pct = (amt / total_exp) * 100
        bar = "█" * int(pct / 5)
        print(f"  {cat:<15} ₹{amt:>8.0f}  {bar} {pct:.1f}%")

    # 3. ML - Predict Next Month Spending
    print("\n🤖 AI Prediction (Linear Regression):")
    monthly_data = expenses.groupby('month_num')['amount'].sum().reset_index()
    if len(monthly_data) >= 2:
        X = monthly_data[['month_num']]
        y = monthly_data['amount']
        model = LinearRegression()
        model.fit(X, y)
        next_month = monthly_data['month_num'].max() + 1
        predicted = model.predict([[next_month]])[0]
        print(f"  Predicted expense for next month: ₹{predicted:.2f}")
        r2 = model.score(X, y)
        print(f"  Model Accuracy (R² Score): {r2:.2f}")

    # 4. Spending Behavior Tag
    avg_monthly = monthly_expense.mean()
    print(f"\n🏷️  Spending Behavior:")
    print(f"  Average monthly expense: ₹{avg_monthly:.2f}")
    if avg_monthly < 5000:
        print("  Tag: 💚 SAVER — You're managing money well!")
    elif avg_monthly < 10000:
        print("  Tag: 🟡 MODERATE — Watch discretionary spending.")
    else:
        print("  Tag: 🔴 HIGH SPENDER — Consider reducing non-essentials.")

    print("\n" + "=" * 55)
    return df, expenses, income, cat_summary, monthly_expense, monthly_income

# ─── Visualization ───────────────────────────────────────────────
def plot_charts(df, expenses, income, cat_summary, monthly_expense, monthly_income):
    fig = plt.figure(figsize=(14, 9))
    fig.suptitle("💰 Financial Tracker - Spending Pattern Analysis",
                 fontsize=16, fontweight='bold', color='#2C3E50')
    gs = gridspec.GridSpec(2, 2, figure=fig, hspace=0.4, wspace=0.35)

    colors = ['#E74C3C','#3498DB','#2ECC71','#F39C12','#9B59B6','#1ABC9C','#E67E22','#95A5A6']

    # Chart 1: Category-wise Pie Chart
    ax1 = fig.add_subplot(gs[0, 0])
    ax1.pie(cat_summary.values, labels=cat_summary.index,
            autopct='%1.1f%%', colors=colors[:len(cat_summary)],
            startangle=140, textprops={'fontsize': 9})
    ax1.set_title("Expense by Category", fontweight='bold')

    # Chart 2: Monthly Income vs Expense Bar Chart
    ax2 = fig.add_subplot(gs[0, 1])
    months = sorted(set(monthly_expense.index) | set(monthly_income.index))
    x = np.arange(len(months))
    w = 0.35
    inc_vals = [monthly_income.get(m, 0) for m in months]
    exp_vals = [monthly_expense.get(m, 0) for m in months]
    ax2.bar(x - w/2, inc_vals, w, label='Income', color='#2ECC71')
    ax2.bar(x + w/2, exp_vals, w, label='Expense', color='#E74C3C')
    ax2.set_xticks(x)
    ax2.set_xticklabels(months, fontsize=9)
    ax2.set_title("Monthly Income vs Expense", fontweight='bold')
    ax2.set_ylabel("Amount (₹)")
    ax2.legend()

    # Chart 3: Spending Trend Line
    ax3 = fig.add_subplot(gs[1, 0])
    monthly_data = expenses.groupby(
        expenses['transaction_date'].dt.to_period('M').astype(str))['amount'].sum()
    ax3.plot(monthly_data.index, monthly_data.values,
             marker='o', color='#E74C3C', linewidth=2, markersize=8)
    ax3.fill_between(range(len(monthly_data)), monthly_data.values,
                     alpha=0.2, color='#E74C3C')
    ax3.set_xticks(range(len(monthly_data)))
    ax3.set_xticklabels(monthly_data.index, fontsize=9)
    ax3.set_title("Monthly Expense Trend", fontweight='bold')
    ax3.set_ylabel("Amount (₹)")

    # Chart 4: Top 5 Expense Bar
    ax4 = fig.add_subplot(gs[1, 1])
    top5 = cat_summary.head(5)
    bars = ax4.barh(top5.index, top5.values, color=colors[:5])
    ax4.set_title("Top 5 Expense Categories", fontweight='bold')
    ax4.set_xlabel("Amount (₹)")
    for bar, val in zip(bars, top5.values):
        ax4.text(bar.get_width() + 50, bar.get_y() + bar.get_height()/2,
                 f'₹{val:.0f}', va='center', fontsize=9)

    plt.savefig("spending_analysis.png", dpi=150, bbox_inches='tight',
                facecolor='#F8F9FA')
    print("  Chart saved as: spending_analysis.png")
    plt.show()

# ─── Main ─────────────────────────────────────────────────────────
if __name__ == "__main__":
    print("\nLoading data...")
    df = get_data()
    df, expenses, income, cat_summary, monthly_expense, monthly_income = \
        spending_pattern_analysis(df)
    print("\nGenerating charts...")
    plot_charts(df, expenses, income, cat_summary, monthly_expense, monthly_income)
