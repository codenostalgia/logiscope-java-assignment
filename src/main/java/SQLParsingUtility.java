import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

import java.io.StringReader;
import java.util.HashMap;

public class SQLParsingUtility {

    static CCJSqlParserManager parserManager;
    static HashMap<String, Integer> column_mapping;

    static {
        parserManager = new CCJSqlParserManager();
        column_mapping = new HashMap<>();
        column_mapping.put("uid", 20);
        column_mapping.put("name", 21);
        column_mapping.put("age", 22);
        column_mapping.put("gender", 23);
        column_mapping.put("status", 30);
        column_mapping.put("msg_ts", 35);
        column_mapping.put("client_ip", 40);
        column_mapping.put("url", 55);
    }

    public static void parse(String query) {
        try {
            Statement statement = parserManager.parse(new StringReader(query));
            Select select = (Select) statement;

            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;

            System.out.println("-----------------------------------");
            FromItem table = plainSelect.getFromItem();
            Expression whereExpression = plainSelect.getWhere();
            System.out.println("Table Name: " + table.toString());
            System.out.println("Where clause: " + whereExpression);
            System.out.println("-----------------------------------");

            if (whereExpression != null) {
                collectOperandsAndOperators(whereExpression);
            }

        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    public static void translate(String query) {
        try {
            Statement statement = parserManager.parse(new StringReader(query));
            Select select = (Select) statement;

            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;

            Expression whereExpression = plainSelect.getWhere();
            System.out.println("-----------------------------------");
            System.out.println("Where clause: " + whereExpression);

            if (whereExpression != null) {
                System.out.println("Translated Where Clause: " + translateQuery(whereExpression));
            }

        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    private static void collectOperandsAndOperators(Expression expression) {
        if (expression instanceof Parenthesis) {
            collectOperandsAndOperators(((Parenthesis) expression).getExpression());
        } else if (expression instanceof LikeExpression) {
            LikeExpression likeExpression = (LikeExpression) expression;

            String operator = "LIKE";
            Column leftColumn = (Column) likeExpression.getLeftExpression();
            String leftOperands = leftColumn.getColumnName();
            String rightOperands = likeExpression.getRightExpression().toString();

            print(operator, leftOperands, rightOperands);

        } else if (expression instanceof Between) {

            Between betweenExpression = (Between) expression;

            String operator = "BETWEEN";
            String leftOperands = betweenExpression.getLeftExpression().toString();
            String rightOperands = betweenExpression.getBetweenExpressionStart() + "  AND  " + betweenExpression.getBetweenExpressionEnd();

            print(operator, leftOperands, rightOperands);

        } else if (expression instanceof InExpression) {

            InExpression inExpression = (InExpression) expression;

            String operator = "IN";
            String leftOperands = inExpression.getLeftExpression().toString();
            ItemsList rightOperands = inExpression.getRightItemsList();

            print(operator, leftOperands, rightOperands.toString());

//            ItemsList rightItems = inExpression.getRightItemsList();
//            if (rightItems instanceof ExpressionList) {
//                ExpressionList expressionList = (ExpressionList) rightItems;
//
//                for (Expression operand : expressionList.getExpressions()) {
//                    collectOperandsAndOperators(operand);
//                }
//            }
        } else if (expression instanceof BinaryExpression) {

            BinaryExpression binaryExpression = (BinaryExpression) expression;

            String operator = binaryExpression.getStringExpression();
            Expression leftOperands = binaryExpression.getLeftExpression();
            Expression rightOperands = binaryExpression.getRightExpression();

            print(operator, leftOperands.toString(), rightOperands.toString());

//            if (operator == "=" || operator == "=") {
//                System.out.println("Manipullation needed");
//            }
            collectOperandsAndOperators(leftOperands);
            collectOperandsAndOperators(rightOperands);

        } else if (expression instanceof Column) {
            System.out.println("Column Name: " + ((Column) expression).getColumnName());
            System.out.println("-----------------------------------");
        } else if (expression instanceof StringValue || expression instanceof LongValue) {
            System.out.println("Value: " + (expression.toString().replace("'", "")));
            System.out.println("-----------------------------------");
        }
    }

    public static Expression translateQuery(Expression expression) {
        if (expression instanceof Parenthesis) {
            return new Parenthesis(translateQuery(((Parenthesis) expression).getExpression()));
        } else if (expression instanceof LikeExpression) {
            LikeExpression likeExpression = (LikeExpression) expression;

            String operator = "LIKE";
            Column leftColumn = (Column) likeExpression.getLeftExpression();
            String leftOperands = leftColumn.getColumnName();
            String rightOperands = likeExpression.getRightExpression().toString();

            // translation
            EqualsTo equalsToExp = new EqualsTo(new Column("id"), new LongValue(column_mapping.get(leftOperands)));

            LikeExpression likeExp = new LikeExpression();
            likeExp.setLeftExpression(new Column("value"));
            likeExp.setRightExpression(likeExpression.getRightExpression());

            AndExpression andExp = new AndExpression();
            andExp.setLeftExpression(equalsToExp);
            andExp.setRightExpression(new Parenthesis(likeExp));

            return new Parenthesis(andExp);

        } else if (expression instanceof Between) {

            Between betweenExpression = (Between) expression;

            String operator = "BETWEEN";
            String leftOperands = betweenExpression.getLeftExpression().toString();
            String rightOperands = betweenExpression.getBetweenExpressionStart() + "  AND  " + betweenExpression.getBetweenExpressionEnd();

            // translation
            EqualsTo equalsToExp = new EqualsTo(new Column("id"), new LongValue(column_mapping.get(leftOperands)));

            Between betweenExp = new Between();
            betweenExp.setLeftExpression(new Column("value"));
            betweenExp.setBetweenExpressionStart(betweenExpression.getBetweenExpressionStart());
            betweenExp.setBetweenExpressionEnd(betweenExpression.getBetweenExpressionEnd());

            AndExpression andExp = new AndExpression();
            andExp.setLeftExpression(equalsToExp);
            andExp.setRightExpression(new Parenthesis(betweenExp));

            return new Parenthesis(andExp);

        } else if (expression instanceof InExpression) {

            InExpression inExpression = (InExpression) expression;

            String operator = "IN";
            String leftOperands = inExpression.getLeftExpression().toString();
            ItemsList rightOperands = inExpression.getRightItemsList();

            ItemsList rightItems = inExpression.getRightItemsList();

            if (rightItems instanceof ExpressionList) {
                ExpressionList expressionList = (ExpressionList) rightItems;

                for (Expression operand : expressionList.getExpressions()) {
                    translateQuery(operand);
                }
            }

            // translation
            EqualsTo equalsToExp = new EqualsTo(new Column("id"), new LongValue(column_mapping.get(leftOperands)));

            InExpression inExp = new InExpression();
            inExp.setLeftExpression(new Column("value"));
            inExp.setRightItemsList(rightItems);

            AndExpression andExp = new AndExpression();
            andExp.setLeftExpression(equalsToExp);
            andExp.setRightExpression(new Parenthesis(inExp));

            return new Parenthesis(andExp);

        } else if (expression instanceof BinaryExpression) {

            BinaryExpression binaryExpression = (BinaryExpression) expression;

            String operator = binaryExpression.getStringExpression();
            Expression leftOperands = binaryExpression.getLeftExpression();
            Expression rightOperands = binaryExpression.getRightExpression();

            Expression leftExp = translateQuery(leftOperands);
            Expression rightExp = translateQuery(rightOperands);

            // translation
            BinaryExpression binaryExp = null;

            if (operator == "AND") {
                binaryExp = new AndExpression();
            } else if (operator == "OR") {
                binaryExp = new OrExpression();
            } else {
                binaryExp = new AndExpression();
            }

            if (binaryExp != null) {
                binaryExp.setLeftExpression(leftExp);
                binaryExp.setRightExpression(rightExp);
            }

            return new Parenthesis(binaryExp);

        } else if (expression instanceof Column) {
            return new EqualsTo(new Column("id"), new LongValue(column_mapping.get(((Column) expression).getColumnName())));

        } else if (expression instanceof StringValue || expression instanceof LongValue) {
            EqualsTo equalsToExp = new EqualsTo(new Column("value"), expression);
            return new Parenthesis(equalsToExp);
        }

        return null;
    }

    private static void print(String operator, String leftOperands, String rightOperands) {
        System.out.println("Operator: " + operator);
        System.out.println("LeftOperand: " + leftOperands);
        System.out.println("RightOperand: " + rightOperands);
        System.out.println("-----------------------------------");
    }
}
