ssCC - Shane Smith's Compiler Compiler
======================================

What is it?
-----------

ssCC is a tool used to create a parser based on the definition of a language.


Can you be more specific?
-------------------------

ssCC will read in language definition files and output java code that parses input written 
in the defined language into a parse tree, which you may then use for your own purposes such
as interpreting or compiling.


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
To mark a token as being internal place a : in front of the name as so:

	:internal: definition

The definition part is written much like regular expressions. Here are the operators you may use:

	*   Match zero or more
	+   Match one or more
	?   Match one or none
	|   Match the pattern on either side, much like an OR
	\   Escape, matches the next character (used to match characters that are operators, such as \+ or \[ or \:)
	\n  Matches a newline characters
	\r  Matches a carriage return character
	\t  Matches a tab character
	\s  Matches a space character
	()  Group patterns
	[abc]  Character class, matches any character within the brackets
	[^abc] Negative character class, matches any character that is _not_ within the brackets (the ^ character must be the first in the character class)
	:tok:  Uses the pattern of the named token to match (does not necessarily need to be an internal token)

Comments may also be written with the # sign either after a definition or on its own line.
	
Also note that spaces and tabs are ignored in the definition, therefore if you want to match "Joe Shmoe" then the definition should be "Joe\sShmoe".


Show me some token definition examples
--------------------------------------

# matching khan, khaan, khaaan, khaaaan...
tok1: kha+n

# matching fun or sun
tok2: [fs]un

# matching "wild cats" or "wild dogs"
tok3: wild \s (cats | dogs)

# matches a quoted string
tok5: " [^"]* "

# matching a variable name (alphanumeric, starts with a letter)
tok4: :alpha: (:alpha: | :digit:)*
:alpha: [abcdefghijklmnopqrstuvwxyz]
:digit: [0123456789]


Alright, now onto language grammars
-----------------------------------

A language's grammar defines the form of strings that belong to the language. In very simple terms, it defines which tokens can follow which tokens.

A grammar is defined by a set of rules written one per line in its own file. A rule is of the following form:

	RuleName -> definition 
	
The definition is comprised of token and rule names along with a few operators. These operators are much again like regular expressions:

	*  Match zero or more
	?  Match one or none
	|  Match the pattern on either side
	() Group patterns
	
There are additionally two special symbols that you may use in the definition:

	\0   Epsilon (an 'empty' match, see examples)
	[>1] Multiple child flag, used to determine whether the rule is included in the parse tree, must be placed at the end of the definition
	
You may again also use the # sign for comments, either after a definition or on it's own line.

