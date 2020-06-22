import React from "react";
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import Box from "@material-ui/core/Box";
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import ListItem from "@material-ui/core/ListItem";
import {coreRequest} from "../../Utilities/Rest";
import {useAuth} from "../../Utilities/Auth";
import Typography from "@material-ui/core/Typography";
import TableContainer from "@material-ui/core/TableContainer";
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import TableBody from "@material-ui/core/TableBody";
import clsx from "clsx";
import {useStyles} from "./styles";


export default function Statistics() {
    const [groupId, setGroupId] = React.useState(-1);
    const [groups, setGroups] = React.useState([{id: -1, name: 'All'}]);
    const [goods, setGoods] = React.useState([]);
    const [statistics, setStatistics] = React.useState({});
    const {token} = useAuth();
    const classes = useStyles();

    React.useEffect(() => {
        coreRequest().get('groups')
            .set('token', token)
            .then(response => {
                setGroups([...response.body, {id: -1, name: 'All'}]);
            })
            .catch(console.error);
        coreRequest().get('statistics')
            .query({group_id: groupId === -1 ? undefined : groupId})
            .set('token', token)
            .then(response => {
                setStatistics({price: response.text});
            })
            .catch(console.error);
        coreRequest().get('goods')
            .query({group_id: groupId === -1 ? undefined : groupId})
            .set('token', token)
            .then(response => {
                setGoods(response.body || [])
            })
            .catch(console.error);
    }, [groupId]);

    function handleChangeGroup(event) {
        setGroupId(event.target.value)
    }

    return (
        <Grid container>
            <Grid item xs={12} md={4}>
                <Box p={1}>
                    <Paper>
                        <Box p={1}>
                            <FormControl fullWidth>
                                <InputLabel id="demo-controlled-open-select-label">Group</InputLabel>
                                <Select
                                    labelId="demo-simple-select-label"
                                    id="demo-simple-select"
                                    value={groupId}
                                    onChange={handleChangeGroup}
                                    name={'group_id'}
                                >
                                    {groups.map(item =>
                                        <MenuItem
                                            key={`group-${item.id}-${item.name}`}
                                            value={item.id}>{item.name}
                                        </MenuItem>)}
                                </Select>
                            </FormControl>
                        </Box>
                    </Paper>
                </Box>
            </Grid>
            <Grid item xs={12} md={8}>
                <Box p={1} style={{height: '100%'}}>
                    <Paper style={{height: '100%'}}>
                        <Box p={1}>
                            <Typography>
                                Total price: {statistics.price || 'not calculated'}
                            </Typography>
                        </Box>
                    </Paper>
                </Box>
            </Grid>
            <Grid item xs={12}>
                <Box p={1}>
                    <Paper>
                        <Box p={1}>
                            <TableContainer>
                                <Table className={classes.table} aria-label="simple table" size={"small"}>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Name</TableCell>
                                            <TableCell align="left">Description</TableCell>
                                            <TableCell align="left">Producer</TableCell>
                                            <TableCell align="left">Price</TableCell>
                                            <TableCell align="left">Quantity</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {goods.map((item) => (
                                            <TableRow
                                                key={`table-item-${item.id}`}
                                            >
                                                <TableCell component="th" scope="row">
                                                    {item.name}
                                                </TableCell>
                                                <TableCell align="left">{item.description}</TableCell>
                                                <TableCell align="left">{item.producer}</TableCell>
                                                <TableCell align="left">{item.price}</TableCell>
                                                <TableCell align="left">{item.quantity}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Box>
                    </Paper>
                </Box>
            </Grid>
        </Grid>
    );
}
