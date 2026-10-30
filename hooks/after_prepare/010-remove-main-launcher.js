
#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const xml2js = require("xml2js");

const projectRoot = process.cwd();

const possiblePaths = [
    path.join(
        projectRoot,
        "platforms/android/app/src/main/AndroidManifest.xml"
    ),
    path.join(
        projectRoot,
        "platforms/android/AndroidManifest.xml"
    )
];

async function main() {

    const manifestPath = possiblePaths.find(fs.existsSync);

    if (!manifestPath) {
        throw new Error("AndroidManifest.xml not found.");
    }

    console.log("[INFO] Manifest:", manifestPath);

    const xml = fs.readFileSync(manifestPath, "utf8");

    const parser = new xml2js.Parser({
        explicitArray: true,
        preserveChildrenOrder: true
    });

    const manifest = await parser.parseStringPromise(xml);

    const application = manifest.manifest.application?.[0];

    if (!application || !application.activity) {
        throw new Error("Application or activity declarations not found.");
    }

    const mainActivity = application.activity.find(activity => {
        const name = activity.$?.["android:name"] || "";
        return name.endsWith(".MainActivity") || name === "MainActivity";
    });

    if (!mainActivity) {
        throw new Error("MainActivity declaration not found.");
    }

    const filters = mainActivity["intent-filter"] || [];

    const originalCount = filters.length;

    mainActivity["intent-filter"] = filters.filter(filter => {

        const actions = filter.action || [];
        const categories = filter.category || [];

        const hasMain = actions.some(
            item => item.$?.["android:name"] ===
                "android.intent.action.MAIN"
        );

        const hasLauncher = categories.some(
            item => item.$?.["android:name"] ===
                "android.intent.category.LAUNCHER"
        );

        return !(hasMain && hasLauncher);
    });

    if (mainActivity["intent-filter"].length === originalCount) {
        throw new Error(
            "MAIN/LAUNCHER filter was not found in MainActivity."
        );
    }

    const builder = new xml2js.Builder({
        xmldec: {
            version: "1.0",
            encoding: "utf-8"
        },
        renderOpts: {
            pretty: true,
            indent: "    "
        }
    });

    const updatedXml = builder.buildObject(manifest);

    fs.writeFileSync(manifestPath, updatedXml, "utf8");

    console.log("[SUCCESS] MainActivity launcher filter removed.");
    console.log("[SUCCESS] MainActivity preserved.");
    console.log("[SUCCESS] Other intent filters preserved.");
}

main().catch(error => {
    console.error("[ERROR]", error.message);
    process.exit(1);
});
