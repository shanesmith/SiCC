ssCC - A Parser Code Generator
==============================

What is it?
-----------

ssCC is a tool used to create a parser based on the definition of a language.


Can you be more specific?
-------------------------

ssCC reads in language definition files and outputs Java code that parses input 
into a parse tree, which may then be use for your own purposes such as interpreting or compiling.


Syntax
======

	ssCC [options] definitions
  
*options*
 
	--package packagename
		includes all of the created classes in the specified package
    
	--prefix prefixname
		prefixes all created classes with the specified string
    
*definitions* is one of the following

	tokendef grammardef
  
	--tokenizer-only tokendef
  
	--parser-only grammardef


examples
--------

This will create a tokenizer and parser with all classes belonging
to the MyProject package.

	ssCC --package MyProject token.def grammar.def
 

This one create only a tokenizer and prefixes each class with "BOB"

	ssCC --prefix BOB --tokenizer-only token.def


Language Definitions
====================

How is the language defined?
----------------------------

A language is defined in two steps and in their own seperate files: first by its tokens, then by its grammar. 


Tell me about tokens and how to define them
--------------------------------------------

Tokens are individual "pieces" of your language which have a type and a value. As an example you may define the english
language has having token types such as punctuation, nouns and verbs with possible values such as "!", "George" and "to run"
respectively.

Tokens are defined one per line in a regular text file. The syntax of a token definition is as follows:

	tokenname: definition
	
The name must be of only alphanumeric characters ('a' to 'b' and '0' to '9') and must start with a letter.

You may also define internal tokens which are not reported by the created tokenizer but may be used in other token definitions.
To mark a token as being internal place a colon in front of the name as such:

	:internal: definition

The definition part is written much like regular expressions. Here are the operators you may use:

	*   Match zero or more
	
	+   Match one or more
	
	?   Match one or none
	
	|   Match the pattern on either side, much like an OR
	
	\   Escape, matches the next character (used to match characters that are operators, such as \+ or \[ or \:)
	
	\\n  Matches a newline characters
	
	\\r  Matches a carriage return character
	
	\\t  Matches a tab character
	
	\\s  Matches a space character
	
	()  Group patterns
	
	[abc]  Character class, matches any character within the brackets
	
	[^abc] Negative character class, matches any character that is _not_ within the brackets
	
	:tok:  Uses the pattern of the named token (not necessarily internal) to match, 
	       note that the token must be defined *before* embeding it

Also, comments may be written with the # sign either after a definition or on its own line.
	
Note that spaces and tabs are ignored in the definition, therefore the definition "J o e" is the same as "Joe", although
most importantly if you want to match the space in "Joe Shmoe" the definition should use the \s special character like so "Joe \s Shmoe".


Show me some token definition examples
--------------------------------------

	# matching "khan", "khaan", "khaaan", "khaaaan"...
	tok1: kha+n

	# matching "fun" or "sun"
	tok2: [fs]un

	# matching "wild cats" or "wild dogs"
	tok3: wild \s (cats | dogs)

	# matches a quoted string
	tok5: " [^"]* "

	# matching a variable name (alphanumeric, starting with a letter)
	:alpha: [abcdefghijklmnopqrstuvwxyz]
	:digit: [0123456789]
	tok4: :alpha: (:alpha: | :digit:)*


Alright, now what about language grammars
-----------------------------------------

A language's grammar defines the form of all strings that belong to the language. In very simple terms, it defines which tokens can follow which tokens.

The (context-free) grammar is defined by a set of rules written one per line in its own text file. Each rule is of the following form:

	RuleName -> definition 
	
The name must again be of only alphanumeric characters ('a' to 'b' and '0' to '9') and must start with a letter.

The definition is comprised of token and rule names along with a few operators. These operators are much again like regular expressions:

	*  Match zero or more
	
	?  Match one or none
	
	|  Match the pattern on either side
	
	() Group patterns
	
Notice that not all operators used in token definitions are available here. 

There are additionally two special symbols that you may use in the definition:

	\\0  Epsilon, an 'empty' match, see examples (only use a single backslash)
	
	[>1] Multiple child flag, must be placed at the end of the definition,
	     signals that the rule should be included in the parse tree only if the node has more than one child,
	     see the discussion about parse trees for more information 
	
Note that literals are not allowed in this definition, only the names of tokens and grammar rules. 

You may again also use the # sign for comments, either after a definition or on it's own line.


Show me some grammar definition examples
----------------------------------------

In these examples we use the convention of having token names in lowercase and
grammar rule names in word-case (ie: first letter of each word in uppercase).

	# matching a phone number of the form "(613) 555-1234"
	PhoneNumber -> AreaCode space FirstPart dash SecondPart
	
	AreaCode -> leftparen threedigits rightparen
	
	FirstPart -> threedigits
	
	SecondPart -> fourdigits
	
	
	# matching a person's name with optional title and optional middle names
	FullName -> NameTitle FirstName MiddleNames LastName
	
	NameTitle -> title | \\0  # equivalent to "title?"
	
	FirstName -> name
	
	MiddleNames -> name*
	
	LastName -> name

	
	# matching a "if" block with possible multiple "elsif" and "else" 
	# (some rule definitions omitted for the sake of brevity)
	IfStatement -> if Condition Block (elsif Condition Block)* (else Block)?
	
	Condition -> leftparen LogicalStatement rightparen 
	
	Block -> leftsquiggly Statement* rightsquiggly


Resulting Code
==============

What does ssCC give me after feeding it the definition files?
-------------------------------------------------------------

ssCC will write several Java classes, of which the two main ones are `Tokenizer` and `Parser`.

As you might have guessed, `Tokenizer` is created based on your token definition, while
`Parser` is created based on the grammar definition.


What does `Tokenizer` do?
-------------------------

The `Tokenizer` class is designed to read in a stream of characters and to output a stream of tokens,
which are encapsulated in the `Token` class, based on your definition.

**Methods**

	Tokenizer(Reader r)
		The constructor takes in an object of type Reader (such as an InputStreamReader)
	
	Token nextToken()
		Returns the next token found in the character stream based on the token definitions file
	
	void pushToken()
		Pushes the last returned token so that the next call to nextToken() returns it, 
		can be called multiple times to push through the history of returned tokens,
		however a limit is imposed and defaulted to 20. See setTokenHistorySize()
	
	int getLineNumber()
		Returns the current line number in the Reader
	
	void setTokenHistorySize(int size)
		Sets how many tokens the Tokenizer will remember in order to use pushToken(), defaulted to 20
	
	int getTokenHistorySize() 
		Returns the number of tokens remembered for pushToken()

What does `Parser` do?
----------------------

The `Parser` class is constructed to read in a stream of `Token`s from a `Tokenizer` and to output
a parse tree based on the given grammar definition. You can then use the parse tree however you like,
most often for interpreting or compiling.

**Methods**

	Parser(iTokenizer t)
		The constructor takes an object that implements the iTokenizer interface, including the Tokenizer generated by ssCC
	
	ASTxxxNode parse()
		Parser's only method returns the parse tree generated from the input of the given Tokenizer based on the grammar definition file.
		To be more specific it returns the top node of the tree of type ASTxxxNode, of which the xxx is defined by the first rule in the grammar.


What about all the other classes that are created?
--------------------------------------------------

Here is a quick outline of each class created, minus exception classes:

* `ASTNode` - Base parse tree node class

* `ASTToken` - A superclass of `ASTNode`, represents a token in the parse tree

* `ASTxxxNode` - A superclass of `ASTNode`, one created for every grammar rule (xxx is replaced by the rule's name)

* `iTokenizer` - An interface implemented by `Tokenizer`

* `Parser` - The main parsing class, takes a `Tokenizer` and outputs a parse tree

* `Token` - A token outputed from `Tokenizer`

* `Tokenizer` - The main tokenizing class, reads in a character stream and outputs a stream of `Token`

* `Visitor` - An interface implementing the visitor pattern, used to traverse the parse tree (read on for more information)


Usage
=====

Enough with the technical, how do I use ssCC?
---------------------------------------------

First we create the two definition files, let's call them "tokens.def" and "grammar.def"


**tokens.def**

	number: [0123456789]+
	
	plus: \+
	minus: -
	multiply: \*
	divide: /
	
	
**grammar.def**

	Equation -> Sum*
	
	Sum -> Term ( (plus | minus) Term )* [>1]
	
	Term -> number ( (multiply | divide) number)* [>1]
	

We then feed them to ssCC like so: `ssCC tokens.def grammar.def`

As mentioned previously we get a handful of java classes in return, among which are `Tokenizer` and `Parser`.

To be able to use these classes in our own program we must create a main class, let's call it `SuperApp`.

Here's a sample of code for `SuperApp`:

	import java.io.*;

	class SuperApp {
		
		public static void main(String args[]) {
		
			try {
			
				Tokenizer tokenizer = new Tokenizer(new InputStreamReader(System.in));
				
				Parser parser = new Parser(tokenizer);
				
				ASTEquationNode equation = parser.parse();
				
				equation.accept(new InterpretorVisitor(), null);
				
			}
			catch (Exception e) {
			
				System.out.println(e);
				
			}
		}
		
	}

You'll notice that in the last line of the `try` block that the parse tree (or rather the parse tree's root node) "accepts"
an object of the type InterpretorVisitor. First we will explain what the parse tree is, then we'll show how to traverse it using a `Visitor`.


What is the parse tree?
-----------------------

The parse tree is a collection of nodes that are attached in a parent/child relationship.

Each rule in your grammar is one possible node type, and each rule and token included in its definition is a child of that rule.

**example**

with the following rules

	A -> B C
	
	B -> x D
	
	C -> y
	
	D -> z
	
the parse tree would be

	    A
	   / \
	  B   C
	 / \   \
	x   D   y
	    |
	    z
	    


What is a `Visitor` and how does it traverse the parse tree?
------------------------------------------------------------


