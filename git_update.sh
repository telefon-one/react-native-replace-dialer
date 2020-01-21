echo get version
VERSION=`npm view react-native-replace-dialer version`
echo v${VERSION}
git add .
git pull
git commit -a -m "v${VERSION}"
git push
#npm version patch
#npm publish
#./npm_publish.sh