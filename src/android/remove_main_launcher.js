const fs = require('fs');
const path = require('path');
module.exports = function(context) {
   const manifestPath = path.join(
       context.opts.projectRoot,
       'platforms',
       'android',
       'app',
       'src',
       'main',
       'AndroidManifest.xml'
   );
   if (!fs.existsSync(manifestPath)) {
       console.log('[AppIconChanger] AndroidManifest.xml not found at: ' + manifestPath);
       return;
   }
   let manifest = fs.readFileSync(manifestPath, 'utf8');
   // Match every <activity ...> ... </activity> block safely across multiple lines
   const activityBlockRegex = /<activity\b[\s\S]*?<\/activity>/g;
   let modified = false;
   manifest = manifest.replace(activityBlockRegex, function(activityBlock) {
       // Check if this specific <activity> is MainActivity
       if (/android:name\s*=\s*["']([^"']*\.?MainActivity)["']/.test(activityBlock)) {
           // Replace LAUNCHER category with DEFAULT inside MainActivity only
           if (activityBlock.includes('android.intent.category.LAUNCHER')) {
               modified = true;
               return activityBlock.replace(
                   /<category\s+android:name\s*=\s*["']android\.intent\.category\.LAUNCHER["']\s*\/>/g,
                   '<category android:name="android.intent.category.DEFAULT" />'
               );
           }
       }
       return activityBlock;
   });
   if (modified) {
       fs.writeFileSync(manifestPath, manifest, 'utf8');
       console.log('[AppIconChanger] Successfully stripped LAUNCHER from MainActivity.');
   } else {
       console.log('[AppIconChanger] MainActivity LAUNCHER was already stripped or not found.');
   }
};
