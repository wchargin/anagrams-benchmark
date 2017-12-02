# coding: utf-8
"""Transliterates a Greek word list to a Phoenician word list.

Disclaimer: I know nothing about the Phoenician language, so tried to
create a reasonable transliteration based on the Wikipedia page. The
accuracy of the transliteration isn't super important, though; we really
just need some natural-like text in the Supplementary Multilingual
Plane.
"""

def translation_table():
  GREEK      = u'αβγδεζηθικλμνξοπρστ'
  PHOENICIAN = u'𐤀𐤁𐤂𐤃𐤄𐤆𐤇𐤈𐤉𐤊𐤋𐤌𐤍𐤎𐤏𐤐𐤑𐤔𐤕'
  assert len(GREEK) == len(PHOENICIAN), (len(GREEK), len(PHOENICIAN))

  identicals = zip('άέήίϊΐός', 'αεηιιιοσ')
  missing = 'υύϋΰφχψωώ'  # no Phoenician equivalents

  result = {}
  for m in missing:
    result[m] = u''
  for (g, p) in zip(GREEK, PHOENICIAN):
    result[g] = p
  for (x, y) in identicals:
    result[x] = result[y]

  return result


def main():
  with open('greek.txt') as infile:
    lines = [line.lower().strip() for line in infile.readlines()]
  TRANSLATION_TABLE = translation_table()
  translated = [u''.join(TRANSLATION_TABLE[c] for c in x) for x in lines]
  with open('phoenician.txt', 'w') as outfile:
    outfile.write(u'\n'.join(translated))
    outfile.write(u'\n')


if __name__ == '__main__':
  main()
