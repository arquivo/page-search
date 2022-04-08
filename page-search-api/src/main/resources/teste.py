import sys
from newspaper import Article
from newspaper import fulltext


# print("This is the name of the program:", sys.argv[0])
# print("This is the first argument:", sys.argv[1])

url = sys.argv[1]
article = Article(url)
article.download()

try:
    print(fulltext(article.html))
except Exception as e:
    print(article.text)