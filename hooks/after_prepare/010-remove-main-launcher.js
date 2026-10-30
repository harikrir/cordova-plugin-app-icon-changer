
const fs = require("fs");
const path = require("path");

const projectRoot = process.cwd();

const manifestPath = path.join(
    projectRoot,
    "platforms",
    "android",
    "app",
    "src",
    "main",
    "AndroidManifest.xml"
);

if (!fs.existsSync(manifestPath)) {
    console.error("AndroidManifest.xml not found: " + manifestPath);
    process.exit(1);
}

let xml = fs.readFileSync(manifestPath, "utf8");

const activityRegex =
    /<activity\b(?=[^>]*android:name=["'][^"']*\.MainActivity["'])[^>]*>[\s\S]*?<\/activity>/;

const match = xml.match(activityRegex);

if (!match) {
    console.error("MainActivity declaration not found.");
    process.exit(1);
}

const activity = match[0];

const updatedActivity = activity.replace(
    /<intent-filter\b(?=[^>]*android:label=["']@string\/launcher_name["'])[^>]*>[\s\S]*?<\/intent-filter>/,
    ""
);

if (updatedActivity === activity) {
    console.error("MainActivity launcher intent filter was not removed.");
    process.exit(1);
}

xml = xml.replace(activity, updatedActivity);

fs.writeFileSync(manifestPath, xml, "utf8");

console.log("Successfully removed MainActivity launcher intent filter.");
