class RenderScheduler {
	constructor() {
		this.isRenderPending = false;
		this._registrations = new Map();
		this._registrationId = 0;
		this._registionTimestamp = 0.0;
		this._renderAnimationFrame = this._renderAnimationFrame.bind(this);
		this._renderQueue = [];
		this._renderTime = 0;
		this.maxFps = 60;
		this.maxRenderTime = 7;
		this._queuePosition = 0;
		this._queueTrimmingSize = 100;
	}

	nextId() {
		const ts = performance.now();
		if (this._registionTimestamp !== ts) {
			this._registionTimestamp = ts;
			this._registionTimeId = 0;
		} else {
			this._registionTimeId++;
		}
		return `${this._registionTimestamp}.${this._registionTimeId}`;
	}

	register(render) {
		var id = this.nextId();
		this._registrations.set(id, {
			id,
			isRenderPending:false,
			render
		});
		return id;
	}

	unregister(id) {
		this._registrations.delete(id);
		return null;
	}

	enqueue(id) {
		var renderEntry = this._registrations.get(id);
		if (!renderEntry) {
			throw new Error(`Render id is not valid ${id}.`);
		}

		if (renderEntry.isRenderPending) {
			return;
		}

		// mark the entry as render pending
		renderEntry.isRenderPending = true;
		this._renderQueue.push(renderEntry);

		// now schedule global render (if not already scheduled)
		if (!this.isRenderPending) {
			this.isRenderPending = true;
			requestAnimationFrame(this._renderAnimationFrame);
		}
	}

	_renderComplete(queuePosition) {
		// update queue
		if (queuePosition > this._queueTrimmingSize) {
			this._renderQueue.splice(0, queuePosition);
			this._queuePosition = 0;
		} else {
			this._queuePosition = queuePosition;
		}

		// are we done, if not re-enqueue
		if (this._renderQueue.length) {
			requestAnimationFrame(this._renderAnimationFrame);
		} else {
			this.isRenderPending = false;
		}
	}

	_renderAnimationFrame() {
		const ts = Date.now();
		const renderTime = this._renderTime;

		if (ts - renderTime < (1000 / this.maxFps)) {
			requestAnimationFrame(this._renderAnimationFrame);
		} else {
			this._renderTime = ts;

			const queueLength = this._renderQueue.length;
			for (let i=this._queuePosition; i<queueLength; i++) {
				const renderEntry = this._renderQueue[i];
				renderEntry.isRenderPending = false;
				renderEntry.render();
				if (Date.now() - ts > this.maxRenderTime) {
					return this._renderComplete(i + 1);
				}
			}

			this._renderComplete(queueLength);
		}
	}
}

export default new RenderScheduler();