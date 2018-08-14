# test
test github
test............
###test1

$P_i = D_k(Ci)\oplus C{i-1}$
=======================
$C_0 = IV$
======================
令A为第N-1块的密文，B为第N块经过key解密后(不是明文)，C为第N块明文

则有$C = A \oplus B \implies C \oplus A \oplus B = 0 \implies C \oplus A \oplus B \oplus X = X$

并且密文A是可控的，所以可以令A=$A \oplus C \oplus X$ ，这样就能令C=X


