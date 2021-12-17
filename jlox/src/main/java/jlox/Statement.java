package jlox;

import java.util.List;

abstract class Statement {
	interface Visitor<R> {
		R visitBlockStatement(Block statement);
		R visitExprStatement(Expr statement);
		R visitPrintStatement(Print statement);
		R visitVarStatement(Var statement);
	}
	static class Block extends Statement {
		final List<Statement> statements;

		Block(List<Statement> statements) {
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStatement(this);
		}
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

	static class Var extends Statement {
		final Token name;
		final Expression init;

		Var(Token name,Expression init) {
			this.name = name;
			this.init = init;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStatement(this);
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
