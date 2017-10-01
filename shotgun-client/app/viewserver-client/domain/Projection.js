export default class Projection{
    constructor(mode, projectionColumns) {
        this.mode = mode;
        this.projectionColumns = projectionColumns;
    }

    static Mode = {
        PROJECTION: 0,
        INCLUSIONARY: 1,
        EXCLUSIONARY: 2
    };
}