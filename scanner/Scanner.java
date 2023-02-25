package ylox.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ylox.main.YLox;

import java.util.HashMap;

import static ylox.scanner.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    //The start and current fields are offsets that index into the string.
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){
        while(!isAtEnd()){
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", NULL, line));

        return tokens;
    }

    private void scanToken(){
        char c = advance();

        switch(c) {
            case '(' : addToken(LEFT_PAREN); break;
            case ')' : addToken(RIGHT_PAREN); break;
            case '{' : addToken(LEFT_BRACE); break;
            case '}' : addToken(RIGHT_BRACE); break;
            case ',' : addToken(COMMA); break;
            case '.' : addToken(DOT); break;
            case '-' : addToken(MINUS); break;
            case '+' : addToken(PLUS); break;
            case ';' : addToken(SEMICOLON); break;
            case '*' : addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;  
            case '/':
                if (match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;    
            default:
            if(isDigit(c)){
                number();
            } else {
                YLox.error(line, "Unexpected character.");
            }
                
            break;

        }
    }

    private void number(){
        while(isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())){
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd()){
            YLox.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private boolean match(char expected){
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
    //Technically, match() is doing lookahead too. advance() and peek() are the fundamental operators and match() combines them.
    //The advance() method consumes the next character in the source file and returns it. Where advance() is for input,
    private char advance(){
        current++;
        return source.charAt(current - 1);
    }
    //addToken() is for output. It grabs the text of the current lexeme and creates a new token for it
    private void addToken(TokenType type){
        tokens.add(new Token(type, source.substring(start, current), type, line));
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}