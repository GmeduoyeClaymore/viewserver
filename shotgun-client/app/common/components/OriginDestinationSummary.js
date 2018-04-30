import React, {Component} from 'react';
import {Grid, Row, Text} from 'native-base';
import {Icon} from 'common/components';
import moment from 'moment';
import shotgun from 'native-base-theme/variables/shotgun';
import {addressToText} from 'common/components/maps/MapUtils';
export class OriginDestinationSummary extends Component{
  constructor(){
    super();
  }

  render(){
    const {origin, destination, distanceAndDuration} = this.props;

    const formatDuration = () => {
      const momentDuration = moment.duration(distanceAndDuration.duration, 'seconds');
      return distanceAndDuration.duration < 3600 ? `${momentDuration.minutes()}mins` : `${momentDuration.hours()}hrs`;
    };

    return <Grid>
      {origin ? <Row>
        <Icon name="pin" paddedIcon originPin/>
        <Text style={styles.originText} numberOfLines={1}>{addressToText(origin)}</Text>
      </Row> : null}
      {distanceAndDuration ? <Row style={styles.timeRow}>
        <Icon name="dashed" style={styles.dashedIcon}/><Text time style={styles.timeText}>
          {distanceAndDuration.distance ? `${Math.round(distanceAndDuration.distance / 1000)}kms` : null}{distanceAndDuration.duration ? ` (${formatDuration()})` : null}
        </Text>
      </Row> : null}
      {destination ? <Row>
        <Icon paddedIcon name="pin" />
        <Text style={styles.originText} numberOfLines={1}>{addressToText(destination)}</Text>
      </Row> : null}
    </Grid>;
  }
}

const styles = {
  timeRow: {
    marginLeft: 6,
    alignItems: 'center'
  },
  originText: {
    alignSelf: 'flex-start'
  },
  timeText: {
    alignSelf: 'flex-start',
    paddingTop: 3,
    paddingLeft: 3
  },
  dashedIcon: {
    color: shotgun.silver,
    fontSize: 30,
    height: 22,
    marginRight: 15
  }
};