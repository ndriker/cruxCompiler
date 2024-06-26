// Generated from c:\Users\ndriker\Documents\School\Homework\winter22\Compilers\cruxCompiler\crux\src\main\antlr4\crux\pt\Crux.g4 by ANTLR 4.8
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CruxLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		INTEGER=1, AND=2, OR=3, NOT=4, IF=5, ELSE=6, LOOP=7, CONTINUE=8, BREAK=9, 
		RETURN=10, TRUE=11, FALSE=12, OPEN_PAREN=13, CLOSE_PAREN=14, OPEN_BRACE=15, 
		CLOSE_BRACE=16, OPEN_BRACKET=17, CLOSE_BRACKET=18, ADD=19, SUB=20, MUL=21, 
		DIV=22, GREATER_EQUAL=23, LESSER_EQUAL=24, NOT_EQUAL=25, EQUAL=26, GREATER_THAN=27, 
		LESS_THAN=28, ASSIGN=29, COMMA=30, SEMICOLON=31, IDENTIFIER=32, WHITESPACES=33, 
		COMMENT=34;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"INTEGER", "AND", "OR", "NOT", "IF", "ELSE", "LOOP", "CONTINUE", "BREAK", 
			"RETURN", "TRUE", "FALSE", "OPEN_PAREN", "CLOSE_PAREN", "OPEN_BRACE", 
			"CLOSE_BRACE", "OPEN_BRACKET", "CLOSE_BRACKET", "ADD", "SUB", "MUL", 
			"DIV", "GREATER_EQUAL", "LESSER_EQUAL", "NOT_EQUAL", "EQUAL", "GREATER_THAN", 
			"LESS_THAN", "ASSIGN", "COMMA", "SEMICOLON", "IDENTIFIER", "WHITESPACES", 
			"COMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, "'&&'", "'||'", "'!'", "'if'", "'else'", "'loop'", "'continue'", 
			"'break'", "'return'", "'true'", "'false'", "'('", "')'", "'{'", "'}'", 
			"'['", "']'", "'+'", "'-'", "'*'", "'/'", "'>='", "'<='", "'!='", "'=='", 
			"'>'", "'<'", "'='", "','", "';'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "INTEGER", "AND", "OR", "NOT", "IF", "ELSE", "LOOP", "CONTINUE", 
			"BREAK", "RETURN", "TRUE", "FALSE", "OPEN_PAREN", "CLOSE_PAREN", "OPEN_BRACE", 
			"CLOSE_BRACE", "OPEN_BRACKET", "CLOSE_BRACKET", "ADD", "SUB", "MUL", 
			"DIV", "GREATER_EQUAL", "LESSER_EQUAL", "NOT_EQUAL", "EQUAL", "GREATER_THAN", 
			"LESS_THAN", "ASSIGN", "COMMA", "SEMICOLON", "IDENTIFIER", "WHITESPACES", 
			"COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public CruxLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Crux.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2$\u00ca\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\3\2\3\2\3\2\7\2K\n\2\f\2\16\2N\13\2\5\2P\n\2\3\3\3\3"+
		"\3\3\3\4\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3"+
		"\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24"+
		"\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\31\3\31\3\31\3\32"+
		"\3\32\3\32\3\33\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3"+
		" \3!\3!\7!\u00b4\n!\f!\16!\u00b7\13!\3\"\6\"\u00ba\n\"\r\"\16\"\u00bb"+
		"\3\"\3\"\3#\3#\3#\3#\7#\u00c4\n#\f#\16#\u00c7\13#\3#\3#\2\2$\3\3\5\4\7"+
		"\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22"+
		"#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C"+
		"#E$\3\2\b\3\2\63;\3\2\62;\5\2C\\aac|\6\2\62;C\\aac|\5\2\13\f\17\17\"\""+
		"\4\2\f\f\17\17\2\u00ce\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2"+
		"\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2"+
		"\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3"+
		"\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2"+
		"\2\2E\3\2\2\2\3O\3\2\2\2\5Q\3\2\2\2\7T\3\2\2\2\tW\3\2\2\2\13Y\3\2\2\2"+
		"\r\\\3\2\2\2\17a\3\2\2\2\21f\3\2\2\2\23o\3\2\2\2\25u\3\2\2\2\27|\3\2\2"+
		"\2\31\u0081\3\2\2\2\33\u0087\3\2\2\2\35\u0089\3\2\2\2\37\u008b\3\2\2\2"+
		"!\u008d\3\2\2\2#\u008f\3\2\2\2%\u0091\3\2\2\2\'\u0093\3\2\2\2)\u0095\3"+
		"\2\2\2+\u0097\3\2\2\2-\u0099\3\2\2\2/\u009b\3\2\2\2\61\u009e\3\2\2\2\63"+
		"\u00a1\3\2\2\2\65\u00a4\3\2\2\2\67\u00a7\3\2\2\29\u00a9\3\2\2\2;\u00ab"+
		"\3\2\2\2=\u00ad\3\2\2\2?\u00af\3\2\2\2A\u00b1\3\2\2\2C\u00b9\3\2\2\2E"+
		"\u00bf\3\2\2\2GP\7\62\2\2HL\t\2\2\2IK\t\3\2\2JI\3\2\2\2KN\3\2\2\2LJ\3"+
		"\2\2\2LM\3\2\2\2MP\3\2\2\2NL\3\2\2\2OG\3\2\2\2OH\3\2\2\2P\4\3\2\2\2QR"+
		"\7(\2\2RS\7(\2\2S\6\3\2\2\2TU\7~\2\2UV\7~\2\2V\b\3\2\2\2WX\7#\2\2X\n\3"+
		"\2\2\2YZ\7k\2\2Z[\7h\2\2[\f\3\2\2\2\\]\7g\2\2]^\7n\2\2^_\7u\2\2_`\7g\2"+
		"\2`\16\3\2\2\2ab\7n\2\2bc\7q\2\2cd\7q\2\2de\7r\2\2e\20\3\2\2\2fg\7e\2"+
		"\2gh\7q\2\2hi\7p\2\2ij\7v\2\2jk\7k\2\2kl\7p\2\2lm\7w\2\2mn\7g\2\2n\22"+
		"\3\2\2\2op\7d\2\2pq\7t\2\2qr\7g\2\2rs\7c\2\2st\7m\2\2t\24\3\2\2\2uv\7"+
		"t\2\2vw\7g\2\2wx\7v\2\2xy\7w\2\2yz\7t\2\2z{\7p\2\2{\26\3\2\2\2|}\7v\2"+
		"\2}~\7t\2\2~\177\7w\2\2\177\u0080\7g\2\2\u0080\30\3\2\2\2\u0081\u0082"+
		"\7h\2\2\u0082\u0083\7c\2\2\u0083\u0084\7n\2\2\u0084\u0085\7u\2\2\u0085"+
		"\u0086\7g\2\2\u0086\32\3\2\2\2\u0087\u0088\7*\2\2\u0088\34\3\2\2\2\u0089"+
		"\u008a\7+\2\2\u008a\36\3\2\2\2\u008b\u008c\7}\2\2\u008c \3\2\2\2\u008d"+
		"\u008e\7\177\2\2\u008e\"\3\2\2\2\u008f\u0090\7]\2\2\u0090$\3\2\2\2\u0091"+
		"\u0092\7_\2\2\u0092&\3\2\2\2\u0093\u0094\7-\2\2\u0094(\3\2\2\2\u0095\u0096"+
		"\7/\2\2\u0096*\3\2\2\2\u0097\u0098\7,\2\2\u0098,\3\2\2\2\u0099\u009a\7"+
		"\61\2\2\u009a.\3\2\2\2\u009b\u009c\7@\2\2\u009c\u009d\7?\2\2\u009d\60"+
		"\3\2\2\2\u009e\u009f\7>\2\2\u009f\u00a0\7?\2\2\u00a0\62\3\2\2\2\u00a1"+
		"\u00a2\7#\2\2\u00a2\u00a3\7?\2\2\u00a3\64\3\2\2\2\u00a4\u00a5\7?\2\2\u00a5"+
		"\u00a6\7?\2\2\u00a6\66\3\2\2\2\u00a7\u00a8\7@\2\2\u00a88\3\2\2\2\u00a9"+
		"\u00aa\7>\2\2\u00aa:\3\2\2\2\u00ab\u00ac\7?\2\2\u00ac<\3\2\2\2\u00ad\u00ae"+
		"\7.\2\2\u00ae>\3\2\2\2\u00af\u00b0\7=\2\2\u00b0@\3\2\2\2\u00b1\u00b5\t"+
		"\4\2\2\u00b2\u00b4\t\5\2\2\u00b3\u00b2\3\2\2\2\u00b4\u00b7\3\2\2\2\u00b5"+
		"\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6B\3\2\2\2\u00b7\u00b5\3\2\2\2"+
		"\u00b8\u00ba\t\6\2\2\u00b9\u00b8\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00b9"+
		"\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00be\b\"\2\2\u00be"+
		"D\3\2\2\2\u00bf\u00c0\7\61\2\2\u00c0\u00c1\7\61\2\2\u00c1\u00c5\3\2\2"+
		"\2\u00c2\u00c4\n\7\2\2\u00c3\u00c2\3\2\2\2\u00c4\u00c7\3\2\2\2\u00c5\u00c3"+
		"\3\2\2\2\u00c5\u00c6\3\2\2\2\u00c6\u00c8\3\2\2\2\u00c7\u00c5\3\2\2\2\u00c8"+
		"\u00c9\b#\2\2\u00c9F\3\2\2\2\b\2LO\u00b5\u00bb\u00c5\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}