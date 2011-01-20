Expression -> Sum
Sum -> Term ((plus|minus) Term)*  [>1]
Term -> Exp ((multiply|divide) Exp)* [>1]  
Exp -> Element (exp Element)*  [>1] 
Element -> number | lparen Sum rparen

