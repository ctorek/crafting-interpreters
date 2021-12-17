package jlox;

import java.util.List;

abstract class Statement {
	interface Visitor<R> {
		R visitExprStatement(Expr statement);
		R visitPrintStatement(Print statement);
	}
	static class Expr extends Statement {
		final Expression expression;

		Expr(Expression expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExprStatement(this);
		}
	}

	static class Print extends Statement {
		final Expression expression;

		Print(Expression expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStatement(this);
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
