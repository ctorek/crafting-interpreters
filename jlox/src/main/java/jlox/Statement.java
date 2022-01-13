package jlox;

import java.util.List;

abstract class Statement {
	interface Visitor<R> {
		R visitFunctionStatement(Function statement);
		R visitIfStatement(If statement);
		R visitBlockStatement(Block statement);
		R visitClassStatement(Class statement);
		R visitExprStatement(Expr statement);
		R visitPrintStatement(Print statement);
		R visitReturnStatement(Return statement);
		R visitVarStatement(Var statement);
		R visitWhileStatement(While statement);
	}
	static class Function extends Statement {
		final Token name;
		final List<Token> params;
		final List<Statement> body;

		Function(Token name,List<Token> params,List<Statement> body) {
			this.name = name;
			this.params = params;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStatement(this);
		}
	}

	static class If extends Statement {
		final Expression condition;
		final Statement thenStmt;
		final Statement elseStmt;

		If(Expression condition,Statement thenStmt,Statement elseStmt) {
			this.condition = condition;
			this.thenStmt = thenStmt;
			this.elseStmt = elseStmt;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStatement(this);
		}
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

	static class Class extends Statement {
		final Token name;
		final List<Statement.Function> methods;

		Class(Token name,List<Statement.Function> methods) {
			this.name = name;
			this.methods = methods;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStatement(this);
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

	static class Return extends Statement {
		final Token keyword;
		final Expression value;

		Return(Token keyword,Expression value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStatement(this);
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

	static class While extends Statement {
		final Expression condition;
		final Statement body;

		While(Expression condition,Statement body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStatement(this);
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
