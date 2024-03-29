import React from "react";
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import TableContainer from "@material-ui/core/TableContainer";
import Table from "@material-ui/core/Table";
import TableRow from "@material-ui/core/TableRow";
import TableHead from "@material-ui/core/TableHead";
import TableCell from "@material-ui/core/TableCell";
import {useStyles} from "./styles";
import TableBody from '@material-ui/core/TableBody';
import IconButton from "@material-ui/core/IconButton";
import AddIcon from '@material-ui/icons/Add';
import EditIcon from '@material-ui/icons/Edit';
import InputBase from "@material-ui/core/InputBase";
import SearchIcon from '@material-ui/icons/Search';
import Box from "@material-ui/core/Box";
import clsx from "clsx";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import ButtonGroup from "@material-ui/core/ButtonGroup";
import {coreRequest} from "../../Utilities/Rest";
import {useConfirmDialog} from "../../Utilities/ConfirmDialog";
import DeleteIcon from '@material-ui/icons/Delete';
import {useAuth} from "../../Utilities/Auth";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import Typography from "@material-ui/core/Typography";
import FormHelperText from '@material-ui/core/FormHelperText';



function DataDialogEditor({onClose, onFinish, open, idata, groups, setGroups}) {
    const defaultData = {
        name: '',
        description: '',
        producer: '',
        quantity: '',
        price: '',
        group_id: '',
    };
    const [backendError, setBackendError] = React.useState("");
    const [data, setData] = React.useState(idata || defaultData);
    const [errors, setErrors] = React.useState({});
    const {token, setToken} = useAuth();
    const classes = useStyles();

    const header = idata ? `Good: ${data && data.name}` : 'Good: New good';

    React.useEffect(() => {
        setData(idata || defaultData);
        setErrors({});
        setBackendError(null);
    }, [idata]);

    React.useEffect(() => {
        coreRequest().get('groups')
            .set('token', token)
            .then(response => {
                setGroups(response.body || [])
            })
            .catch(console.error);
    }, [idata, open]);

    function handleCheckFields() {
        let noError = true;
        let newerrors = errors;

        function sfe(field, error) {
            newerrors = {...newerrors, [field]: error};
        }

        function checkEmpty(fields) {
            for (const key of fields) {
                const item = data[key];
                if (!item) {
                    if (!newerrors[key]) sfe(key, `Field can not be empty`);
                    noError = false;
                } else {
                    if (newerrors[key] === `Field can not be empty`) sfe(key, null);
                }
            }
        }

        function checkNumber(fields) {
            for (const key of fields) {
                const item = data[key];
                if (isNaN(+item)) {
                    if (!newerrors[key]) sfe(key, `Field must be numeric type`);
                    noError = false;
                } else {
                    if (newerrors[key] === `Field must be numeric type`) sfe(key, null);
                }
            }
        }

        checkEmpty(['name', 'quantity', 'price', 'group_id']);
        checkNumber(['quantity', 'price', 'group_id']);
        setErrors(newerrors);
        return noError;
    }

    function handleAdd() {
        if (!handleCheckFields()) return;

        coreRequest()
            .post('goods')
            .send(data)
            .set('token', token)
            .then(response => {
                setBackendError(null);
                onFinish && onFinish();
            })
            .catch(error => {
                switch (error.status) {
                    case 401:
                        setToken(null);
                        break;
                    case 409:
                        setBackendError("Good's name already exists");
                        break;
                    default:
                        setBackendError("Error");
                        break;
                }
            });
    }

    function handleEdit() {
        if (!handleCheckFields()) return;
        coreRequest()
            .put('good')
            .send(data)
            .query({id: +idata.id})
            .set('token', token)
            .then(response => {
                setBackendError(null);
                onFinish && onFinish();
            })
            .catch(error => {
                switch (error.status) {
                    case 401:
                        setToken(null);
                        break;
                    case 409:
                        setBackendError("Good's name already exists");
                        break;
                    default:
                        setBackendError("Error");
                        break;
                }
            });


    }

    function handleInput(event) {
        function makeInt(obj, keys) {
            for (const key of keys) {
                const item = obj[key];
                if (item[item.length-1] !== '.') {
                    obj = {...obj, [key]: Math.abs(+item) || item};
                }
            }
            return obj;
        }

        event.persist();
        setData(last => makeInt({...last, [event.target.name]: event.target.value}, ["quantity", "price", "group_id"]));
    }

    return (
        <Dialog onClose={onClose} aria-labelledby="simple-dialog-title" open={open}>
            <DialogTitle id="simple-dialog-title">{header}</DialogTitle>
            <List>
                {backendError &&
                <ListItem>
                    <Typography className={classes.error}>
                        {backendError}
                    </Typography>
                </ListItem>
                }
                <ListItem>
                    <TextField
                        fullWidth
                        label={'Name'}
                        name={'name'}
                        value={data.name}
                        onChange={handleInput}
                        error={errors.name}
                        helperText={errors.name && errors.name}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        label={'Description'}
                        name={'description'}
                        value={data.description}
                        multiline
                        rowsMax={10}
                        onChange={handleInput}
                        error={errors.description}
                        helperText={errors.description && errors.description}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        label={'Producer'}
                        name={'producer'}
                        value={data.producer}
                        onChange={handleInput}
                        error={errors.producer}
                        helperText={errors.producer && errors.producer}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        label={'Quantity'}
                        name={'quantity'}
                        value={data.quantity}
                        onChange={handleInput}
                        error={errors.quantity}
                        helperText={errors.quantity && errors.quantity}
                    />
                </ListItem>
                <ListItem>
                    <TextField
                        fullWidth
                        label={'Price'}
                        name={'price'}
                        value={data.price}
                        onChange={handleInput}
                        error={errors.price}
                        helperText={errors.price && errors.price}
                    />
                </ListItem>
                <ListItem>
                    <FormControl fullWidth error={!!errors.group_id}>
                        <InputLabel id="demo-controlled-open-select-label">Group</InputLabel>
                        <Select
                            labelId="demo-simple-select-label"
                            id="demo-simple-select"
                            value={data.group_id}
                            onChange={handleInput}
                            name={'group_id'}
                        >
                            {groups.map(item => <MenuItem key={`group-${item.id}-${item.name}`}
                                                          value={item.id}>{item.name}</MenuItem>)}
                        </Select>
                        {errors.group_id && <FormHelperText error>{errors.group_id}</FormHelperText>}
                    </FormControl>
                </ListItem>
            </List>
            <ListItem>
                <ButtonGroup fullWidth variant={'text'}>
                    <Button onClick={idata ? handleEdit : handleAdd}>
                        {idata ? 'Edit' : 'Add'}
                    </Button>
                    <Button onClick={onClose}>
                        Cancel
                    </Button>
                </ButtonGroup>
            </ListItem>
        </Dialog>
    );
}

export default function Goods() {

    const [goods, setGoods] = React.useState([]);
    const [rowId, setRowId] = React.useState(0);
    const [dataDialogOpen, setDataDialogOpen] = React.useState(false);
    const [search, setSearch] = React.useState('');
    const [isNewRow, setIsNewRow] = React.useState(false);
    const [groups, setGroups] = React.useState([]);
    const {token, setToken} = useAuth();
    const classes = useStyles();
    const confirm = useConfirmDialog();

    function handleUpdate() {
        coreRequest()
            .get(`goods`)
            .query({query: search ? search : undefined})
            .set('token', token)
            .then(response => {
                setGoods(response.body);
            })
            .catch(error => {
                switch (error.message) {
                    case 401:
                        setToken(null);
                        break;
                    default:
                        break;
                }
            });

    }

    function handleDelete() {
        coreRequest()
            .delete(`good`)
            .query({id: rowId})
            .set('token', token)
            .then(response => handleUpdate())
            .catch(error => {
                switch (error.message) {
                    case 401:
                        setToken(null);
                        break;
                    default:
                        break;
                }
            });
    }

    function handleSearchInput(event) {
        setSearch(event.target.value);
    }

    React.useEffect(() => {
        handleUpdate();
    }, []);

    React.useEffect(() => {
        if (goods.length) {
            setRowId(goods[0] && goods[0].id);
        }
    }, [goods]);

    return (
        <React.Fragment>
            <Grid container>
                <Grid item xs={12}>
                    <Paper>
                        <Box p={1}>
                            <div style={{display: 'flex', justifyContent: 'flex-end'}}>
                                <div style={{display: 'inline-block'}}>
                                    <InputBase
                                        className={classes.input}
                                        placeholder="Search good"
                                        inputProps={{'aria-label': 'search google maps'}}
                                        onChange={handleSearchInput}
                                        value={search}
                                    />
                                    <IconButton
                                        className={classes.iconButton}
                                        aria-label="search"
                                        onClick={handleUpdate}
                                    >
                                        <SearchIcon/>
                                    </IconButton>
                                </div>
                            </div>

                            <TableContainer>
                                <Table className={classes.table} aria-label="simple table" size={"small"}>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Name</TableCell>
                                            <TableCell align="right">Description</TableCell>
                                            <TableCell align="right">Price</TableCell>
                                            <TableCell align="right">Producer</TableCell>
                                            <TableCell align="right">Quantity</TableCell>
                                            <TableCell align="right">Group</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {goods.map((item) => {
                                            const group = groups.find(element => element.id === item.group_id) || {};
                                            const groupName = group.name || 'No group';
                                            return (
                                                <TableRow
                                                    key={`table-item-${item.id}`}
                                                    className={clsx(item.id === rowId && classes.activeTable)}
                                                    onClick={event => setRowId(item.id)}
                                                >
                                                    <TableCell component="th" scope="row">
                                                        {item.name}
                                                    </TableCell>
                                                    <TableCell align="right">{item.description}</TableCell>
                                                    <TableCell align="right">{item.price}</TableCell>
                                                    <TableCell align="right">{item.producer}</TableCell>
                                                    <TableCell align="right">{item.quantity}</TableCell>
                                                    <TableCell align="right">{groupName}</TableCell>
                                                </TableRow>
                                            )
                                        })}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <IconButton onClick={event => {
                                setDataDialogOpen(true);
                                setIsNewRow(true);
                            }}>
                                <AddIcon/>
                            </IconButton>
                            <IconButton
                                onClick={event => setDataDialogOpen(true)}
                                disabled={!goods.length}
                            >
                                <EditIcon/>
                            </IconButton>
                            <IconButton
                                disabled={!goods.length}
                                onClick={event => confirm(handleDelete, {title: `Are you sure you want delete good?`})}>
                                <DeleteIcon/>
                            </IconButton>
                        </Box>
                    </Paper>
                </Grid>
            </Grid>
            <DataDialogEditor
                open={dataDialogOpen}
                idata={isNewRow ? undefined : goods.find(item => item.id === rowId)}
                onClose={() => {
                    setDataDialogOpen(false);
                    setIsNewRow(false);
                }}
                onFinish={() => {
                    setDataDialogOpen(false);
                    handleUpdate();
                    setIsNewRow(false);
                }}
                groups={groups}
                setGroups={setGroups}
            />
        </React.Fragment>
    );
}