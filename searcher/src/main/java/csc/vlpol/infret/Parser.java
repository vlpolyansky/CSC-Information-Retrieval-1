package csc.vlpol.infret;


import java.util.ArrayList;

import static csc.vlpol.infret.Utils.isEnglish;
import static csc.vlpol.infret.Utils.isRussian;

class Parser {

    private String[] lexems;
    private int index = 0;

    Parser(String line) {
        lexems = line.split("\\s+");
    }

    FullQuery parseFulQuery() throws QueryException {
        if (index == lexems.length) {
            return null;
        }
        ArrayList<SubQuery> list = new ArrayList<>();
        list.add(readSubQuery());
        boolean and = false;
        boolean or = false;
        while (index < lexems.length) {
            String op = lexems[index];
            if (op.equalsIgnoreCase("and")) {
                and = true;
            } else if (op.equalsIgnoreCase("or")) {
                or = true;
            } else {
                throw new QueryException("'and'/'or' expected, but '" + op + "' found");
            }
            ++index;
            if (and && or) {
                throw new QueryException("'and' & 'or' operations are not supported together");
            }
            list.add(readSubQuery());
        }
        return new FullQuery(list, and);
    }

    private SubQuery readSubQuery() throws QueryException {
        if (index == lexems.length) {
            throw new QueryException("SubQuery is empty");
        }
        ArrayList<String> words = new ArrayList<>();
        ArrayList<QueryOperator> infix = new ArrayList<>();
        words.add(readWord());
        while (index < lexems.length) {
            String op = lexems[index];
            if (op.startsWith("/")) {
                infix.add(readOperator());
                words.add(readWord());
            } else {
                break;
            }
        }
        return new SubQuery(words, infix);
    }

    private String readWord() throws QueryException {
        if (index == lexems.length) {
            throw new QueryException("Word expected but eol found");
        }
        if (!check(lexems[index])) {
            throw new QueryException("Wrong word: '" + lexems[index] + "'");
        }
        return lexems[index++];
    }

    private boolean check(String word) {
        boolean rus = false;
        boolean eng = false;
        for (int i = 0; i < word.length(); ++i) {
            if (isRussian(word.charAt(i))) {
                if (eng) {
                    return false;
                }
                rus = true;
            } else if (isEnglish(word.charAt(i))) {
                if (rus) {
                    return false;
                }
                eng = true;
            } else {
                return false;
            }
        }
        return true;
    }

    private QueryOperator readOperator() throws QueryException {
        if (index == lexems.length) {
            throw new QueryException("Operator but eol found");
        }
        String op = lexems[index];
        if (!op.startsWith("/")) {
            throw new QueryException("'/' expected");
        }
        try {
            QueryOperator ret;
            if (op.startsWith("/+")) {
                ret = new QueryOperator(Integer.parseInt(op.substring(2)), 1);
            } else if (op.startsWith("/-")) {
                ret = new QueryOperator(Integer.parseInt(op.substring(2)), -1);
            } else if (op.startsWith("/")) {
                ret = new QueryOperator(Integer.parseInt(op.substring(1)), 0);
            } else {
                throw new QueryException("'/' expected");
            }
            ++index;
            return ret;
        } catch (QueryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new QueryException("Exception reading operator", ex);
        }

    }

}

class FullQuery {
    public ArrayList<SubQuery> list;
    public boolean and;

    public FullQuery(ArrayList<SubQuery> list, boolean and) {
        this.list = list;
        this.and = and;
    }
}

class SubQuery {
    public ArrayList<String> words;
    public ArrayList<QueryOperator> infix;

    public SubQuery(ArrayList<String> words, ArrayList<QueryOperator> infix) {
        this.words = words;
        this.infix = infix;
    }
}

class QueryOperator {
    public int number;
    public int direction;

    QueryOperator(int number, int direction) {
        this.number = number;
        this.direction = direction;
    }
}

class QueryException extends Exception {

    public QueryException(String message) {
        super(message);
    }

    QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}